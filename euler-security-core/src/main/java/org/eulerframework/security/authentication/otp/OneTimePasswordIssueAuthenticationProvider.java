/*
 * Copyright 2013-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.security.authentication.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Collections;

/**
 * {@link AuthenticationProvider} that handles OTP ticket issue requests:
 * resolves the applicable {@link OneTimePasswordPolicy} and the recipient, delegates
 * ticket minting (OTP generation and persistence) to the
 * {@link OneTimePasswordService} and dispatches delivery to the configured
 * {@link OneTimePasswordChannel}.
 * <p>
 * Delivery is asynchronous: the issue response is written once the ticket is
 * persisted and never reflects the delivery outcome. Clients recover from a
 * lost delivery by re-requesting after {@code retry_after}.
 * <p>
 * When an {@link OneTimePasswordTestAccountSupport} is configured, recipients on its
 * whitelist receive the configured fixed OTP and real delivery is skipped;
 * verification is unaffected.
 *
 * @see OneTimePasswordIssueAuthenticationToken
 */
public class OneTimePasswordIssueAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(OneTimePasswordIssueAuthenticationProvider.class);

    private final OneTimePasswordPolicyResolver policyResolver;
    private final OneTimePasswordChannel oneTimePasswordChannel;
    private final OneTimePasswordService ticketService;
    private final OneTimePasswordRecipientResolver recipientResolver;

    private OneTimePasswordTestAccountSupport testAccountSupport;

    /**
     * @param policyResolver    must not be {@code null}
     * @param oneTimePasswordChannel        must not be {@code null}
     * @param ticketService     must not be {@code null}
     * @param recipientResolver may be {@code null}; when absent, requests
     *                          carrying an {@code identity_id} are rejected
     *                          with {@link OneTimePasswordInvalidIdentityIdException}
     */
    public OneTimePasswordIssueAuthenticationProvider(OneTimePasswordPolicyResolver policyResolver,
                                                OneTimePasswordChannel oneTimePasswordChannel,
                                                OneTimePasswordService ticketService,
                                                OneTimePasswordRecipientResolver recipientResolver) {
        Assert.notNull(policyResolver, "policyResolver must not be null");
        Assert.notNull(oneTimePasswordChannel, "oneTimePasswordChannel must not be null");
        Assert.notNull(ticketService, "ticketService must not be null");
        this.policyResolver = policyResolver;
        this.oneTimePasswordChannel = oneTimePasswordChannel;
        this.ticketService = ticketService;
        this.recipientResolver = recipientResolver;
    }

    /**
     * Configure the optional test-account whitelist. When set, requests whose
     * resolved recipient matches one of its entries receive the configured
     * fixed OTP and skip real delivery. Pass {@code null} to disable
     * (default).
     */
    public void setTestAccountSupport(OneTimePasswordTestAccountSupport testAccountSupport) {
        this.testAccountSupport = testAccountSupport;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(OneTimePasswordIssueAuthenticationToken.class, authentication,
                () -> "Only OneTimePasswordIssueAuthenticationToken is supported");
        OneTimePasswordIssueAuthenticationToken token = (OneTimePasswordIssueAuthenticationToken) authentication;

        OneTimePasswordIssueRequest request = new OneTimePasswordIssueRequest(
                token.getChannel(), token.getRecipient(), token.getIdentityId(),
                token.getPurpose());

        // 1. Resolve policy
        OneTimePasswordPolicy policy = this.policyResolver.resolve(request);
        Assert.notNull(policy, "OneTimePasswordPolicyResolver must not return null");

        // 2. Resolve recipient
        String recipient = resolveRecipient(token);

        // 3. Decide whether this is a test account: it receives a fixed OTP
        //    and skips real delivery.
        boolean testAccount = this.testAccountSupport != null
                && this.testAccountSupport.isTestAccount(recipient);

        // 4. Mint and persist the ticket. The ticket service generates the OTP
        //    value (or uses the fixed one for test accounts) and returns the
        //    ticket carrying both the id and the value.
        GenerateOneTimePasswordRequest generationRequest = new GenerateOneTimePasswordRequest(
                token.getChannel(), recipient, token.getPurpose(),
                policy.otpLength(), policy.expiresIn(),
                testAccount ? this.testAccountSupport.getFixedOtp() : null);
        OneTimePassword ticket;
        try {
            ticket = this.ticketService.generate(generationRequest);
        } catch (RuntimeException e) {
            throw new AuthenticationServiceException("Failed to persist OTP ticket", e);
        }
        String ticketId = ticket.ticketId();
        String otp = ticket.otp();

        // 5. Deliver - skipped for test accounts; only a single WARN line is emitted
        if (testAccount) {
            logger.warn("OTP test account hit: channel='{}' recipient='{}' purpose='{}' fixed-otp='{}' - real delivery skipped",
                    token.getChannel(), recipient, token.getPurpose(), otp);
        } else {
            OneTimePasswordDelivering delivering = new OneTimePasswordDelivering(
                    token.getChannel(), recipient, token.getPurpose(), otp, policy.expiresIn());
            try {
                this.oneTimePasswordChannel.send(delivering)
                        // Fire-and-forget: the response is written before delivery completes, so
                        // failures can only be recorded here; clients recover by re-requesting
                        // after retry_after.
                        .whenComplete((_, failure) -> {
                            if (failure != null) {
                                logger.error("OTP delivery failed: ticket='{}' channel='{}' recipient='{}' purpose='{}'",
                                        ticketId, token.getChannel(), recipient, token.getPurpose(), failure);
                            }
                        });
            } catch (OneTimePasswordChannelNotFoundException e) {
                throw new OneTimePasswordUnsupportedChannelException(e.getMessage(), e);
            } catch (RuntimeException e) {
                throw new AuthenticationServiceException("OTP delivery dispatch failed", e);
            }
        }

        logger.debug("Issued OTP ticket id='{}' channel='{}' purpose='{}'",
                ticketId, token.getChannel(), token.getPurpose());

        OneTimePasswordIssueResult result = new OneTimePasswordIssueResult(ticketId, policy.expiresIn(), policy.retryAfter());
        return OneTimePasswordIssueAuthenticationToken.authenticated(
                token.getChannel(), recipient, token.getIdentityId(),
                token.getPurpose(),
                result, Collections.emptyList());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OneTimePasswordIssueAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private String resolveRecipient(OneTimePasswordIssueAuthenticationToken token) {
        if (StringUtils.hasText(token.getRecipient())) {
            return token.getRecipient();
        }
        // identity_id path
        if (this.recipientResolver == null) {
            throw new OneTimePasswordInvalidIdentityIdException(
                    "No OneTimePasswordRecipientResolver bean is registered to resolve identity_id");
        }
        try {
            String resolved = this.recipientResolver.resolve(token.getIdentityId());
            if (!StringUtils.hasText(resolved)) {
                throw new OneTimePasswordInvalidIdentityIdException(
                        "OneTimePasswordRecipientResolver returned no recipient for identity_id");
            }
            return resolved;
        } catch (OneTimePasswordInvalidIdentityIdException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new OneTimePasswordInvalidIdentityIdException(
                    "OneTimePasswordRecipientResolver failed to resolve identity_id", e);
        }
    }
}
