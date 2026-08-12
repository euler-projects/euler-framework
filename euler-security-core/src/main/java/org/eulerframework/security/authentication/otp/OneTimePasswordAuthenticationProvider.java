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

import org.eulerframework.common.util.StringUtils;
import org.eulerframework.security.core.EulerUser;
import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.core.identity.UserIdentity;
import org.eulerframework.security.core.identity.UserIdentityService;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.core.userdetails.RandomUsernameGenerator;
import org.eulerframework.security.provisioning.jit.JitProvisioningPolicy;
import org.eulerframework.security.provisioning.jit.JitProvisioningPolicyResolver;
import org.eulerframework.security.util.UserDetailsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * {@link AuthenticationProvider} that authenticates a user from a submitted
 * one-time password: consumes the ticket through {@link OtpTicketService},
 * resolves the verified recipient to a user through the identity SPI -
 * auto-provisioning a fresh user when the recipient is unknown - and loads
 * the resolved user's details.
 *
 * @see OneTimePasswordAuthenticationToken
 */
public class OneTimePasswordAuthenticationProvider implements AuthenticationProvider {

    /**
     * Hard-coded {@code OTP channel -> identity_type} mapping. Values
     * use the public {@code identity_type} namespace surfaced on the
     * {@code /user/identities} REST surface; the identity SPI keys on
     * the same namespace.
     */
    private static final Map<String, String> CHANNEL_TO_IDENTITY_TYPE = Map.of(
            "sms", "phone",
            "email", "email"
    );

    private static final Map<String, String> CHANNEL_TO_RAW_SUB_PARAM_NAME = Map.of(
            "sms", "phone",
            "email", "email"
    );

    private final Logger logger = LoggerFactory.getLogger(OneTimePasswordAuthenticationProvider.class);

    private final OtpTicketService otpTicketService;
    /**
     * Identity SPI used to reverse-resolve the OTP recipient back to a
     * user and to auto-provision a binding when the recipient is
     * unknown.
     */
    private final UserIdentityService userIdentityService;
    private final EulerUserService eulerUserService;
    private final JitProvisioningPolicyResolver jitProvisioningPolicyResolver;

    public OneTimePasswordAuthenticationProvider(OtpTicketService otpTicketService,
                                                 UserIdentityService userIdentityService,
                                                 EulerUserService eulerUserService,
                                                 JitProvisioningPolicyResolver jitProvisioningPolicyResolver) {
        Assert.notNull(otpTicketService, "otpTicketService must not be null");
        Assert.notNull(userIdentityService, "userIdentityService must not be null");
        Assert.notNull(eulerUserService, "eulerUserService must not be null");
        Assert.notNull(jitProvisioningPolicyResolver, "jitProvisioningPolicyResolver must not be null");
        this.otpTicketService = otpTicketService;
        this.userIdentityService = userIdentityService;
        this.eulerUserService = eulerUserService;
        this.jitProvisioningPolicyResolver = jitProvisioningPolicyResolver;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(OneTimePasswordAuthenticationToken.class, authentication,
                () -> "Only OneTimePasswordAuthenticationToken is supported");
        OneTimePasswordAuthenticationToken otpAuthenticationToken = (OneTimePasswordAuthenticationToken) authentication;

        // 1. Atomically consume the OTP ticket. consume() performs the OTP
        //    value match.
        OtpVerification verification;
        try {
            verification = this.otpTicketService.consume(
                    (String) otpAuthenticationToken.getPrincipal(),
                    otpAuthenticationToken.getOtp(),
                    null);
        } catch (RuntimeException e) {
            throw new AuthenticationServiceException("OTP verification failed", e);
        }
        if (verification == null) {
            throw new BadCredentialsException("OTP verification failed");
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Consumed OTP ticket id='{}' channel='{}'",
                    verification.ticketId(), verification.channel());
        }

        // 2. Reverse-resolve (identity_type, recipient) -> userId via
        //    the identity SPI. The flow does not know how a per-type
        //    backend derives its persisted `subject` field (phone hash /
        //    email normalize+hash / wechat openid pass-through / ...);
        //    it merely picks the identity_type and asks who owns the
        //    raw value.
        String identityType = resolveIdentityType(verification.channel());
        String rawSubjectParamName = resolveRawSubjectParamName(verification.channel());
        String rawSubject = verification.recipient();
        UserIdentity identity = this.userIdentityService
                .findUserIdentityByRawSubject(identityType, rawSubject)
                .orElseGet(() -> autoProvisionUser(identityType, rawSubjectParamName, rawSubject));

        // 3. Load the resolved user's details.
        EulerUser eulerUser = this.eulerUserService.loadUserById(identity.getUserId());
        EulerUserDetails userDetails = UserDetailsUtils.toEulerUserDetails(eulerUser);
        if (userDetails == null || CollectionUtils.isEmpty(userDetails.getAuthorities())) {
            throw new AuthenticationServiceException("Failed to load user after auto-provision");
        }

        OneTimePasswordAuthenticationToken result = OneTimePasswordAuthenticationToken.authenticated(
                userDetails, identity, userDetails.getAuthorities());
        result.setDetails(otpAuthenticationToken.getDetails());
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OneTimePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private static String resolveIdentityType(String channel) {
        String identityType = CHANNEL_TO_IDENTITY_TYPE.get(channel);
        if (identityType == null) {
            throw new OtpUnsupportedChannelException("Unsupported OTP channel: " + channel, null);
        }
        return identityType;
    }

    private static String resolveRawSubjectParamName(String channel) {
        String attributeName = CHANNEL_TO_RAW_SUB_PARAM_NAME.get(channel);
        if (attributeName == null) {
            throw new OtpUnsupportedChannelException("Unsupported OTP channel: " + channel, null);
        }
        return attributeName;
    }

    /**
     * Auto-provision a fresh user and bind {@code (identityType, rawSubject)}
     * to it via the pre-verified prototype entry
     * {@link UserIdentityService#createUserIdentity(String, UserIdentity)}.
     *
     * <p>An OTP login whose recipient is unknown is treated as an implicit
     * signup, subject to the {@link JitProvisioningPolicy} resolved for the
     * identity type. The username is generated through
     * {@link RandomUsernameGenerator#generate()} (form
     * {@code user_<base64url12>}) so the recipient never leaks into the
     * local username; the password is a {@code {noop}}-prefixed random
     * string (OTP-only login, no password authentication path); authorities
     * come from the policy.
     *
     * <p>This provider handles {@code identity_type ∈ {phone, email}}. For
     * both, the raw subject is attached to the prototype as an extension
     * attribute whose key equals the {@code identity_type} string itself
     * (e.g. {@code "phone"} for the phone backend); the backend reads it
     * back under the same key.
     */
    private UserIdentity autoProvisionUser(String identityType, String rawSubjectParamName, String rawSubject) {
        JitProvisioningPolicy jitProvisioning = this.jitProvisioningPolicyResolver.resolve(identityType);
        if (!jitProvisioning.isEnabled()) {
            throw new BadCredentialsException(
                    "Unknown recipient and JIT provisioning is disabled");
        }
        EulerUserDetails newUser = EulerUserDetails.builder()
                .username(RandomUsernameGenerator.generate())
                .password("{noop}" + StringUtils.randomString(32))
                .authorities(jitProvisioning.defaultAuthoritiesArray())
                .build();
        EulerUser createdUser = this.eulerUserService.createUser(newUser);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("JIT-provisioned user '{}' for OTP identity_type='{}'",
                    createdUser.getUserId(), identityType);
        }
        UserIdentity prototype = UserIdentity.builder()
                .identityType(identityType)
                .property(rawSubjectParamName, rawSubject)
                .build();
        return this.userIdentityService.createUserIdentity(createdUser.getUserId(), prototype);
    }
}
