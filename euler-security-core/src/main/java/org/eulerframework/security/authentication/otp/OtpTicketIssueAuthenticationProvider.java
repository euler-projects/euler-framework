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

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;

/**
 * {@link AuthenticationProvider} that drives the OTP issue flow:
 * <ol>
 *     <li>Resolves the {@link OtpPolicy} via {@link OtpPolicyResolver}.</li>
 *     <li>Resolves the recipient: either the explicit {@code recipient} from
 *         the request or, when only {@code identity_id} was supplied, the
 *         output of {@link OtpRecipientResolver}.</li>
 *     <li>Generates a numeric OTP via {@link OtpGenerator}.</li>
 *     <li>Mints a ticket id ({@code ot_} + 22-char URL-safe random) and persists
 *         the resulting {@link OtpTicket} via {@link OtpTicketService}.</li>
 *     <li>Delegates delivery to the configured {@link OtpChannel} (typically
 *         {@link DelegatingOtpChannel}).</li>
 * </ol>
 * <p>
 * When an {@link OtpTestAccountSupport} is configured and the resolved
 * recipient matches a test account, the OTP value is replaced by the configured
 * fixed value, real channel delivery is skipped, and a single {@code WARN}
 * line is emitted. Verification is unaffected - the ticket persisted with the
 * fixed OTP is matched by
 * {@link OtpTicketService#consume(String, String, String, String)} via the
 * usual plaintext compare path.
 *
 * @see OtpTicketIssueAuthenticationToken
 * @see OtpTestAccountSupport
 */
public class OtpTicketIssueAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(OtpTicketIssueAuthenticationProvider.class);

    private static final String TICKET_ID_PREFIX = "ot_";
    private static final int TICKET_ID_RANDOM_BYTES = 16; // -> 22 base64url chars

    private final OtpPolicyResolver policyResolver;
    private final OtpGenerator otpGenerator;
    private final OtpChannel otpChannel;
    private final OtpTicketService ticketService;
    private final OtpRecipientResolver recipientResolver;

    private OtpTestAccountSupport testAccountSupport;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * @param policyResolver    must not be {@code null}
     * @param otpGenerator      must not be {@code null}
     * @param otpChannel        must not be {@code null} (typically a
     *                          {@link DelegatingOtpChannel})
     * @param ticketService     must not be {@code null}
     * @param recipientResolver may be {@code null} - when {@code null},
     *                          requests carrying an {@code identity_id} are
     *                          rejected with {@link OtpInvalidIdentityIdException}
     */
    public OtpTicketIssueAuthenticationProvider(OtpPolicyResolver policyResolver,
                                                OtpGenerator otpGenerator,
                                                OtpChannel otpChannel,
                                                OtpTicketService ticketService,
                                                OtpRecipientResolver recipientResolver) {
        Assert.notNull(policyResolver, "policyResolver must not be null");
        Assert.notNull(otpGenerator, "otpGenerator must not be null");
        Assert.notNull(otpChannel, "otpChannel must not be null");
        Assert.notNull(ticketService, "ticketService must not be null");
        this.policyResolver = policyResolver;
        this.otpGenerator = otpGenerator;
        this.otpChannel = otpChannel;
        this.ticketService = ticketService;
        this.recipientResolver = recipientResolver;
    }

    /**
     * Configure the optional test-account whitelist. When set, requests whose
     * resolved recipient matches one of its entries receive the configured
     * fixed OTP and skip real delivery. Pass {@code null} to disable
     * (default).
     */
    public void setTestAccountSupport(OtpTestAccountSupport testAccountSupport) {
        this.testAccountSupport = testAccountSupport;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(OtpTicketIssueAuthenticationToken.class, authentication,
                () -> "Only OtpTicketIssueAuthenticationToken is supported");
        OtpTicketIssueAuthenticationToken token = (OtpTicketIssueAuthenticationToken) authentication;

        OtpIssueRequest request = new OtpIssueRequest(
                token.getChannel(), token.getRecipient(), token.getIdentityId(),
                token.getPurpose(), token.getCodeChallenge(), token.getCodeChallengeMethod());

        // 1. Resolve policy
        OtpPolicy policy = this.policyResolver.resolve(request);
        Assert.notNull(policy, "OtpPolicyResolver must not return null");

        // 2. Resolve recipient
        String recipient = resolveRecipient(token);

        // 3. Generate the OTP value (or use the fixed value for test accounts)
        boolean testAccount = this.testAccountSupport != null
                && this.testAccountSupport.isTestAccount(recipient);
        String otp = testAccount
                ? this.testAccountSupport.getFixedOtp()
                : this.otpGenerator.generate(policy.otpLength());

        // 4. Mint a ticket id and persist the ticket
        String ticketId = generateTicketId();
        Instant now = Instant.now();
        OtpTicket ticket = new OtpTicket(
                ticketId,
                token.getChannel(),
                recipient,
                token.getPurpose(),
                otp,
                token.getCodeChallenge(),
                token.getCodeChallengeMethod(),
                now.plus(policy.expiresIn()),
                0,
                false);
        try {
            this.ticketService.save(ticket);
        } catch (RuntimeException e) {
            throw new AuthenticationServiceException("Failed to persist OTP ticket", e);
        }

        // 5. Deliver - skipped for test accounts; only a single WARN line is emitted
        if (testAccount) {
            logger.warn("OTP test account hit: channel='{}' recipient='{}' purpose='{}' fixed-otp='{}' - real delivery skipped",
                    token.getChannel(), recipient, token.getPurpose(), otp);
        } else {
            OtpDelivering delivering = new OtpDelivering(
                    token.getChannel(), recipient, token.getPurpose(), otp, policy.expiresIn());
            try {
                this.otpChannel.send(delivering);
            } catch (OtpChannelNotFoundException e) {
                throw new OtpUnsupportedChannelException(e.getMessage(), e);
            } catch (OtpDeliveryException e) {
                throw new OtpDeliveryFailedException("OTP delivery failed", e);
            } catch (RuntimeException e) {
                throw new AuthenticationServiceException("OTP delivery failed", e);
            }
        }

        logger.debug("Issued OTP ticket id='{}' channel='{}' purpose='{}'",
                ticketId, token.getChannel(), token.getPurpose());

        OtpIssueResult result = new OtpIssueResult(ticketId, policy.expiresIn(), policy.retryAfter());
        return OtpTicketIssueAuthenticationToken.authenticated(
                token.getChannel(), recipient, token.getIdentityId(),
                token.getPurpose(), token.getCodeChallenge(), token.getCodeChallengeMethod(),
                result, Collections.emptyList());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OtpTicketIssueAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private String resolveRecipient(OtpTicketIssueAuthenticationToken token) {
        if (StringUtils.hasText(token.getRecipient())) {
            return token.getRecipient();
        }
        // identity_id path
        if (this.recipientResolver == null) {
            throw new OtpInvalidIdentityIdException(
                    "No OtpRecipientResolver bean is registered to resolve identity_id");
        }
        try {
            String resolved = this.recipientResolver.resolve(token.getIdentityId());
            if (!StringUtils.hasText(resolved)) {
                throw new OtpInvalidIdentityIdException(
                        "OtpRecipientResolver returned no recipient for identity_id");
            }
            return resolved;
        } catch (OtpInvalidIdentityIdException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new OtpInvalidIdentityIdException(
                    "OtpRecipientResolver failed to resolve identity_id", e);
        }
    }

    private String generateTicketId() {
        byte[] bytes = new byte[TICKET_ID_RANDOM_BYTES];
        this.secureRandom.nextBytes(bytes);
        String suffix = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return TICKET_ID_PREFIX + suffix;
    }
}
