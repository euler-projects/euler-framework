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
import org.springframework.util.StringUtils;

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
 *     <li>Takes the key's own {@code kid} from the submitted JWK &mdash; the caller chooses it,
 *         and it is what a later assertion has to name to reach this key &mdash; and stores the
 *         pair via {@link AppAttestInstanceKeyRegistrationService#saveRegistration}, which is
 *         insert-only: a key ID already taken is a conflict rather than an overwrite. Hands back
 *         the key as persisted, so the response reports the store rather than the request</li>
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
 * The {@code kid} is the caller's to choose and is opaque: a handle on a row, not a statement
 * about the key. Nothing here derives it from the key material and nothing downstream needs it to
 * be derivable &mdash; what binds an account to a key is the key's own thumbprint, which the
 * identity backend computes and stores, and what verifies a signature is the key material. Keeping
 * the identifier out of both is what lets it stay a plain name. It is required, because it is the
 * only thing a later assertion can quote to reach this row.
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

    /**
     * Bound on a caller-chosen key ID, matching the {@code jwk_kid} column it is stored in.
     */
    private static final int MAX_KEY_ID_LENGTH = 128;

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
        String jwkKid = requireKeyId(publicKey);

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

        // 5. Register the key under the App Attest KEY that just authenticated the request, filed
        //    by the key ID the caller chose for it. What comes back is the registration as
        //    persisted, read back by the store, which is what the response reports.
        AppAttestInstanceKeyRegistration keyRegistration = this.instanceKeyRegistrationService.saveRegistration(
                new AppAttestInstanceKeyRegistration(appAttestKid, jwkKid, publicKey.toJSONString()));

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

    /**
     * The identifier the caller chose for its own key, which is what the row is filed under and
     * what a later assertion has to name to reach it.
     * <p>
     * Bounded because it becomes a primary key column, and screened for control characters because
     * it is echoed into a JSON response and written into a log line, neither of which should have
     * to sanitise what this accepted. Nothing else about it is constrained: it is a name, and what
     * makes it usable is that it is unique, which the registry enforces when it stores the row.
     */
    private static String requireKeyId(JWK publicKey) {
        String keyId = publicKey.getKeyID();
        if (!StringUtils.hasText(keyId)) {
            throw new InvalidInstanceKeyException(
                    "The JWK must carry a kid: it is the identifier the key is registered under");
        }
        if (keyId.length() > MAX_KEY_ID_LENGTH) {
            throw new InvalidInstanceKeyException(
                    "The JWK kid must not exceed " + MAX_KEY_ID_LENGTH + " characters");
        }
        for (int i = 0; i < keyId.length(); i++) {
            char c = keyId.charAt(i);
            if (c < 0x20 || c == 0x7F) {
                throw new InvalidInstanceKeyException("The JWK kid must not contain control characters");
            }
        }
        return keyId;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AppAttestInstanceKeyRegistrationAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
