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

package org.eulerframework.security.authentication.apple;

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.core.userdetails.EulerAppleAppAttestUserDetailsService;
import org.eulerframework.security.core.userdetails.UserDetailsNotFountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;

/**
 * {@link AuthenticationProvider} that handles App Attest registration (attestation) requests.
 * <p>
 * This provider performs the attestation validation flow by delegating to
 * {@link AppleAppAttestValidationService}, then handles user creation/lookup:
 * <ol>
 *     <li>Consumes the one-time challenge via {@link ChallengeService}</li>
 *     <li>Delegates attestation validation to {@link AppleAppAttestValidationService#validateAttestation}</li>
 *     <li>Loads or creates the user via {@link EulerAppleAppAttestUserDetailsService}</li>
 * </ol>
 *
 * @see AppAttestRegistrationAuthenticationToken
 */
public class AppAttestRegistrationAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(AppAttestRegistrationAuthenticationProvider.class);

    private final ChallengeService challengeService;
    private final AppleAppAttestValidationService validationService;
    private final EulerAppleAppAttestUserDetailsService userDetailsService;

    public AppAttestRegistrationAuthenticationProvider(ChallengeService challengeService,
                                                       AppleAppAttestValidationService validationService,
                                                       EulerAppleAppAttestUserDetailsService userDetailsService) {
        Assert.notNull(challengeService, "challengeService must not be null");
        Assert.notNull(validationService, "validationService must not be null");
        Assert.notNull(userDetailsService, "userDetailsService must not be null");
        this.challengeService = challengeService;
        this.validationService = validationService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(@Nonnull Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(org.eulerframework.security.authentication.apple.AppAttestRegistrationAuthenticationToken.class, authentication,
                () -> "Only AppAttestRegistrationAuthenticationToken is supported");
        org.eulerframework.security.authentication.apple.AppAttestRegistrationAuthenticationToken token = (org.eulerframework.security.authentication.apple.AppAttestRegistrationAuthenticationToken) authentication;

        String keyId = token.getKeyId();
        String attestation = token.getAttestation();
        String challenge = token.getChallenge();

        // 1. Consume the one-time challenge
        if (!this.challengeService.consumeChallenge(challenge)) {
            throw new BadCredentialsException("Invalid or expired challenge");
        }

        try {
            // 2. Validate attestation and save registration via the delegated validation service
            AppAttestRegistration registration = this.validationService.validateAttestation(keyId, attestation, challenge);

            logger.debug("App Attest registration succeeded for keyId: {}", keyId);

            // 3. Load or create the user
            AppleAppAttestUser attestUser = new AppleAppAttestUser(
                    keyId, registration.getTeamId(), registration.getBundleId(), registration.getPublicKey());
            UserDetails user = loadOrCreateUser(attestUser);

            // 4. Return authenticated token
            return org.eulerframework.security.authentication.apple.AppAttestRegistrationAuthenticationToken.authenticated(user, keyId, user.getAuthorities());
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationServiceException("App Attest registration failed", e);
        }
    }

    private UserDetails loadOrCreateUser(AppleAppAttestUser attestUser) {
        try {
            return this.userDetailsService.loadUserByAppleAppAttestUser(attestUser);
        } catch (UserDetailsNotFountException ex) {
            logger.debug("No existing user found for keyId '{}', creating new user", attestUser.getKeyId());
            return this.userDetailsService.createUser(attestUser);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return org.eulerframework.security.authentication.apple.AppAttestRegistrationAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
