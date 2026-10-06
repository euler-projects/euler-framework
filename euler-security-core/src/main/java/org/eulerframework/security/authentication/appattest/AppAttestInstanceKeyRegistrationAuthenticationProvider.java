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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.appattest.apple.AppleAppAttestValidationService;
import org.eulerframework.security.util.JwkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.text.ParseException;

/**
 * {@link AuthenticationProvider} that registers a public key an authenticated App instance
 * generated itself.
 * <p>
 * Like {@link AppAttestAttestationRegistrationAuthenticationProvider} this is an <b>App
 * instance</b> operation rather than a user authentication: it establishes no
 * {@code SecurityContext} / login state, creates no user and produces no subject. Flow:
 * <ol>
 *     <li>Consumes the one-time challenge via {@link ChallengeService}</li>
 *     <li>Authenticates the caller with its App Attest assertion via
 *         {@link AppleAppAttestValidationService#validateAssertion}. An assertion's
 *         authenticator data carries no credential ID, so the key ID has to be supplied
 *         by the caller to locate the registered KEY.</li>
 *     <li>Registers the key under the App Attest KEY that authenticated the request, which
 *         is what identifies the App instance</li>
 *     <li>Derives the key's own {@code kid} from the key material rather than trusting a
 *         caller-chosen one, and stores the pair via
 *         {@link AppAttestInstanceKeyRegistrationService#saveRegistration}, which is idempotent &mdash; a repeat
 *         registration of the same key updates the existing entry &mdash; and hands back the
 *         key as persisted, so the response reports the store rather than the request</li>
 * </ol>
 * <p>
 * The instance is identified by its App Attest KEY and by nothing else. In particular this does
 * not consult the OAuth2 {@code client_id} that KEY may be bound to: that binding lives in the
 * OAuth2 domain and is what a <em>consumer</em> of a registered key needs in order to trust an
 * assertion's {@code iss}, not something registration here depends on. An instance that has not
 * completed dynamic client registration can still register keys; whether any consumer will then
 * accept them is that consumer's rule.
 * <p>
 * The submitted key is checked before the challenge is consumed, so a request with an unusable
 * body is answered as the request error it is ({@link InvalidInstanceKeyException}) without
 * burning a challenge the client then has to fetch again.
 * <p>
 * Deriving the {@code kid} as the RFC 7638 thumbprint is what makes registration idempotent and
 * the key addressable: two registrations of the same key material collide on the same
 * {@code (appAttestKid, jwkKid)}, so a retry cannot produce a second entry, and a client that
 * later quotes the {@code kid} is quoting the key's own fingerprint. Because the collision proves
 * the two submissions are the same key, the later one is free to replace the earlier &mdash; see
 * {@link AppAttestInstanceKeyRegistrationService#saveRegistration}.
 * <p>
 * No proof of possession is demanded here. The private key cannot leave the platform's secure
 * area, so possession proves itself on first use, when a signature has to verify; requiring a
 * second proof at registration would only repeat that check earlier.
 *
 * @see AppAttestInstanceKeyRegistrationAuthenticationToken
 * @see AppAttestInstanceKeyRegistration
 */
public class AppAttestInstanceKeyRegistrationAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger =
            LoggerFactory.getLogger(AppAttestInstanceKeyRegistrationAuthenticationProvider.class);

    private final ChallengeService challengeService;
    private final AppleAppAttestValidationService validationService;
    private final AppAttestInstanceKeyRegistrationService instanceKeyRegistrationService;

    public AppAttestInstanceKeyRegistrationAuthenticationProvider(
            ChallengeService challengeService,
            AppleAppAttestValidationService validationService,
            AppAttestInstanceKeyRegistrationService instanceKeyRegistrationService) {
        Assert.notNull(challengeService, "challengeService must not be null");
        Assert.notNull(validationService, "validationService must not be null");
        Assert.notNull(instanceKeyRegistrationService, "instanceKeyRegistrationService must not be null");
        this.challengeService = challengeService;
        this.validationService = validationService;
        this.instanceKeyRegistrationService = instanceKeyRegistrationService;
    }

    @Override
    public Authentication authenticate(@Nonnull Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(AppAttestInstanceKeyRegistrationAuthenticationToken.class, authentication,
                () -> "Only AppAttestInstanceKeyRegistrationAuthenticationToken is supported");
        AppAttestInstanceKeyRegistrationAuthenticationToken token =
                (AppAttestInstanceKeyRegistrationAuthenticationToken) authentication;

        // 1. Check the submitted key before spending anything on the credential, so a bad
        //    body costs the client a retry rather than a fresh challenge.
        JWK submittedKey = parsePublicKey(token.getPublicKeyJson());
        JWK publicKey = toRegistrablePublicKey(submittedKey);

        // 2. Consume the one-time challenge
        if (!this.challengeService.consumeChallenge(token.getChallenge())) {
            throw new BadCredentialsException("Invalid or expired challenge");
        }

        // 3. Authenticate the App instance with its assertion
        AppAttestAttestationRegistration registration;
        try {
            registration = this.validationService.validateAssertion(
                    token.getKeyId(), token.getAssertion(), token.getChallenge());
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationServiceException("App Attest assertion validation failed", e);
        }

        // 4. Register under the App Attest KEY that just authenticated the request. Taken from
        //    the verified registration rather than from the request header, so the instance a
        //    key is filed under is the one that was proved rather than the one that was claimed.
        String appAttestKid = registration.getKeyId();

        // 5. Derive the key's own ID from its material and register the pair. What comes back is
        //    the registration as persisted, read back by the store, which is what the response
        //    reports.
        String jwkKid = JwkUtils.computeThumbprint(publicKey);
        AppAttestInstanceKeyRegistration keyRegistration = this.instanceKeyRegistrationService.saveRegistration(
                new AppAttestInstanceKeyRegistration(
                        appAttestKid, jwkKid, JwkUtils.withKeyId(publicKey, jwkKid).toJSONString()));

        if (logger.isDebugEnabled()) {
            logger.debug("Registered key '{}' for App Attest KEY '{}'",
                    keyRegistration.jwkKid(), keyRegistration.appAttestKid());
        }

        return AppAttestInstanceKeyRegistrationAuthenticationToken.registered(keyRegistration);
    }

    private static JWK parsePublicKey(String publicKeyJson) {
        try {
            return JWK.parse(publicKeyJson);
        } catch (ParseException | RuntimeException e) {
            throw new InvalidInstanceKeyException("The request body is not a valid JWK: " + e.getMessage(), e);
        }
    }

    /**
     * Reduce the submitted key to what may be registered: public-only, and of a key type
     * whose signatures this framework can verify.
     */
    private static JWK toRegistrablePublicKey(JWK submittedKey) {
        try {
            return JwkUtils.toPublicJwk(submittedKey);
        } catch (JOSEException e) {
            throw new InvalidInstanceKeyException("The JWK cannot be registered: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AppAttestInstanceKeyRegistrationAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
