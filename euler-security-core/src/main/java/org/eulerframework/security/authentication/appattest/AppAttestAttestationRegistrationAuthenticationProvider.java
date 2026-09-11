
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

package org.eulerframework.security.authentication.appattest;

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.appattest.apple.AppleAppAttestValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;

/**
 * {@link AuthenticationProvider} that verifies a device attestation and registers
 * the device KEY.
 * <p>
 * This is a <b>device registration</b> operation, not a user authentication: it
 * establishes no {@code SecurityContext} / login state and creates no user. Flow:
 * <ol>
 *     <li>Consumes the one-time challenge via {@link ChallengeService}</li>
 *     <li>Delegates attestation validation and KEY persistence to
 *         {@link AppleAppAttestValidationService#validateAttestation}</li>
 *     <li>Returns an authenticated token whose principal is the verified
 *         {@link AppAttestAttestationRegistration} and which carries no authorities</li>
 * </ol>
 *
 * @see AppAttestAttestationRegistrationAuthenticationToken
 */
public class AppAttestAttestationRegistrationAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(AppAttestAttestationRegistrationAuthenticationProvider.class);

    private final ChallengeService challengeService;
    private final AppleAppAttestValidationService validationService;

    public AppAttestAttestationRegistrationAuthenticationProvider(ChallengeService challengeService,
                                                                  AppleAppAttestValidationService validationService) {
        Assert.notNull(challengeService, "challengeService must not be null");
        Assert.notNull(validationService, "validationService must not be null");
        this.challengeService = challengeService;
        this.validationService = validationService;
    }

    @Override
    public Authentication authenticate(@Nonnull Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(AppAttestAttestationRegistrationAuthenticationToken.class, authentication,
                () -> "Only DeviceAttestRegistrationAuthenticationToken is supported");
        AppAttestAttestationRegistrationAuthenticationToken token = (AppAttestAttestationRegistrationAuthenticationToken) authentication;

        String attestation = token.getAttestation();
        String challenge = token.getChallenge();

        // 1. Consume the one-time challenge
        if (!this.challengeService.consumeChallenge(challenge)) {
            throw new BadCredentialsException("Invalid or expired challenge");
        }

        try {
            // 2. Validate attestation and save the KEY registration via the delegated
            // validation service. The key ID is derived from the attestation. No user is
            // created: this endpoint only registers the device KEY.
            AppAttestAttestationRegistration registration = this.validationService.validateAttestation(attestation, challenge);

            logger.debug("Device attestation registration succeeded for keyId: {}", registration.getKeyId());

            // 3. Return an authenticated token carrying the verified device registration.
            return AppAttestAttestationRegistrationAuthenticationToken.registered(registration);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationServiceException("Device attestation registration failed", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AppAttestAttestationRegistrationAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
