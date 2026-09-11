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
package org.eulerframework.security.oauth2.server.authorization.authentication;

import java.util.Map;
import java.util.Optional;

import jakarta.annotation.Nonnull;
import org.eulerframework.security.oauth2.core.EulerOAuth2ClientAttestationType;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.EulerOAuth2ClientAttestationAuthenticationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.apple.AppleAppAttestValidationService;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerOAuth2ErrorCodes;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;

/**
 * An {@link AuthenticationProvider} that verifies Client Attestation data for
 * {@code attest_jwt_client_auth} clients, as defined in
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-08.html">
 * draft-ietf-oauth-attestation-based-client-auth-08</a>.
 * <p>
 * This provider is registered with {@code OAuth2ClientAuthenticationFilter} and handles
 * the {@code attest_jwt_client_auth} authentication method (Section 6.3 / 13.4 of the draft).
 * It works in tandem with {@link EulerOAuth2ClientAttestationAuthenticationConverter},
 * which collects raw attestation data from the request without any parsing or verification.
 * <p>
 * The provider performs all verification and resolution:
 * <ol>
 *   <li>Dispatches PoP verification by {@code popType}:
 *       <ul>
 *         <li>{@code jwt}: delegates to {@link EulerOAuth2ClientAttestationVerifier} which handles
 *             kid extraction, key lookup, and PoP JWT verification.</li>
 *         <li>{@code apple_app_attest}: consumes the one-time challenge via
 *             {@link ChallengeService}, then validates the App Attest data via
 *             {@link AppleAppAttestValidationService}. The {@code attestation} and
 *             {@code assertion} parameters are not mutually exclusive:
 *             <ul>
 *               <li>{@code attestation} only &mdash; registers the device KEY (idempotently)
 *                   and authenticates the client, with the key ID derived from the
 *                   attestation's credential ID. This only authenticates a client that
 *                   already exists, i.e. a STATIC client pre-provisioned for the app; a
 *                   DYNAMIC app has no {@code client_id} until its per-key client is
 *                   dynamically registered, so it is rejected with
 *                   {@code unauthorized_client}. The KEY registration is kept either way:
 *                   it is valid and is what the client needs next, so it can proceed
 *                   straight to dynamic client registration without re-attesting.</li>
 *               <li>{@code assertion} only &mdash; fast path for an already-registered device;
 *                   requires {@code kid}, since an assertion carries no credential ID.</li>
 *               <li>both &mdash; registers the device KEY first, then validates the assertion
 *                   against the derived key ID, completing registration and device
 *                   verification in a single request.</li>
 *             </ul>
 *             Challenge consumption is decoupled from verification (both verifications only
 *             use the challenge as nonce input), so the challenge is consumed exactly once
 *             and a single challenge may back both an attestation and an assertion derived
 *             from it. The two combinations involving an attestation are compatibility paths;
 *             see {@link EulerOAuth2ParameterNames#ATTESTATION}. Only available when Apple App
 *             Attest is enabled (requires a non-null
 *             {@link AppleAppAttestValidationService}).</li>
 *       </ul>
 *   </li>
 *   <li>Resolves the {@code client_id} from the verification result.</li>
 *   <li>Looks up the {@link RegisteredClient} and verifies it supports
 *       {@code attest_jwt_client_auth}.</li>
 *   <li>Validates RFC 6749 {@code client_id} consistency if the request carried one.</li>
 * </ol>
 * <p>
 * After successful authentication, the verified {@link AppAttestAttestationRegistration}
 * is preserved as the authenticated token's credentials for downstream components.
 *
 * @see EulerOAuth2ClientAttestationAuthenticationConverter
 * @see EulerOAuth2ClientAttestationVerifier
 * @see EulerClientAuthenticationMethod#ATTEST_JWT_CLIENT_AUTH
 */
public final class EulerOAuth2ClientAttestationAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(EulerOAuth2ClientAttestationAuthenticationProvider.class);

    private final RegisteredClientRepository registeredClientRepository;
    private final EulerOAuth2ClientAttestationVerifier oauth2ClientAttestationVerifier;
    private final ChallengeService challengeService;
    private AppleAppAttestValidationService appleAppAttestValidationService;

    public EulerOAuth2ClientAttestationAuthenticationProvider(
            RegisteredClientRepository registeredClientRepository,
            EulerOAuth2ClientAttestationVerifier oauth2ClientAttestationVerifier,
            ChallengeService challengeService) {
        Assert.notNull(registeredClientRepository, "registeredClientRepository must not be null");
        Assert.notNull(oauth2ClientAttestationVerifier, "oauth2ClientAttestationVerifier must not be null");
        Assert.notNull(challengeService, "challengeService must not be null");
        this.registeredClientRepository = registeredClientRepository;
        this.oauth2ClientAttestationVerifier = oauth2ClientAttestationVerifier;
        this.challengeService = challengeService;
    }

    public void setAppleAppAttestValidationService(AppleAppAttestValidationService appleAppAttestValidationService) {
        this.appleAppAttestValidationService = appleAppAttestValidationService;
    }

    public EulerOAuth2ClientAttestationVerifier getOauth2ClientAttestationVerifier() {
        return oauth2ClientAttestationVerifier;
    }

    @Override
    public Authentication authenticate(@Nonnull Authentication authentication) throws AuthenticationException {
        OAuth2ClientAuthenticationToken clientAuthentication = (OAuth2ClientAuthenticationToken) authentication;

        if (!EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH
                .equals(clientAuthentication.getClientAuthenticationMethod())) {
            return null;
        }

        Map<String, Object> additionalParams = clientAuthentication.getAdditionalParameters();
        EulerOAuth2ClientAttestationType clientAttestationType = (EulerOAuth2ClientAttestationType) additionalParams
                .get(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_TYPE);

        final String resolvedClientId;
        final AppAttestAttestationRegistration appRegistration;

        if (EulerOAuth2ClientAttestationType.JWT.equals(clientAttestationType)) {
            String attestationJwt = (String) additionalParams.get(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION);
            String attestationPopJwt = (String) additionalParams.get(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP);

            if (attestationPopJwt == null) {
                throw invalidClientAttestation(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP);
            }

            EulerOAuth2ClientAttestationVerifier.PopVerificationResult result = attestationJwt == null
                    ? this.oauth2ClientAttestationVerifier.verify(attestationPopJwt)
                    : this.oauth2ClientAttestationVerifier.verify(attestationJwt, attestationPopJwt);

            appRegistration = result.registration();

            resolvedClientId = result.clientId();
        } else if (EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST.equals(clientAttestationType)) {
            if (this.appleAppAttestValidationService == null) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION,
                                "APP_ATTEST attestation type is not supported; "
                                        + "enable euler.security.authentication.app-attest to use this attestation type", null));
            }

            // The converter normalizes both carriages onto the canonical header keys, so this
            // provider is transport-agnostic; only the deprecated attestation has no header analog.
            String challenge = (String) additionalParams.get(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE);

            if (!StringUtils.hasText(challenge)) {
                throw invalidClientAttestation(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE);
            }

            String attestation = (String) additionalParams.get(EulerOAuth2ParameterNames.ATTESTATION);
            String assertion = (String) additionalParams.get(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_ASSERTION);

            if (!StringUtils.hasText(attestation) && !StringUtils.hasText(assertion)) {
                // Defensive: the converter already rejects this. Reject before consuming, so a
                // malformed request does not burn an otherwise valid one-time challenge.
                throw invalidClientAttestation(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_ASSERTION);
            }

            // Consume the one-time challenge exactly once, before any verification. Consumption
            // is decoupled from verification (both only use the challenge as nonce input), so a
            // single challenge may legitimately back both an attestation and an assertion
            // generated from it, which is what makes the combined request below possible.
            if (!this.challengeService.consumeChallenge(challenge)) {
                throw invalidClientAttestation(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE);
            }

            if (StringUtils.hasText(attestation)) {
                // Compatibility path; see EulerOAuth2ParameterNames#ATTESTATION.
                // Register the device KEY (idempotent). The key ID is derived from the
                // attestation's credentialId, so it is not read from the request here.
                AppAttestAttestationRegistration registered =
                        this.appleAppAttestValidationService.validateAttestation(attestation, challenge);

                // Fail fast, before verifying any accompanying assertion.
                requireBoundClientId(registered);

                if (StringUtils.hasText(assertion)) {
                    // attestation + assertion in one request: the device KEY is registered
                    // first and the assertion is then verified against the derived key ID,
                    // so a single request completes both registration and device
                    // verification. Redundant in strict terms, but supported.
                    if (logger.isDebugEnabled()) {
                        logger.debug("Both attestation and assertion supplied; verifying the assertion "
                                + "against the keyId '{}' derived from the attestation", registered.getKeyId());
                    }
                    appRegistration = this.appleAppAttestValidationService.validateAssertion(
                            registered.getKeyId(), assertion, challenge);
                } else {
                    appRegistration = registered;
                }
            } else {
                // Assertion-only fast path for an already-registered device; the assertion is
                // guaranteed present by the guard above. An assertion's authenticator data
                // carries no credentialId, so the key ID must be supplied to locate the
                // registered device.
                String keyId = (String) additionalParams.get(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_KID);
                if (!StringUtils.hasText(keyId)) {
                    throw invalidClientAttestation(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_KID);
                }
                appRegistration = this.appleAppAttestValidationService.validateAssertion(keyId, assertion, challenge);
                requireBoundClientId(appRegistration);
            }

            // Resolve client_id directly from the attestation registration: it has been
            // bound at registration time (deterministic base64url(SHA-256(appId)) for
            // STATIC clients, or the dynamically issued identifier for DYNAMIC clients).
            resolvedClientId = appRegistration.getClientId();
        } else {
            throw invalidClientAttestation(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_TYPE);
        }

        if (resolvedClientId == null) {
            throw invalidClientAttestation(OAuth2ParameterNames.CLIENT_ID);
        }

        // Draft Section 6.3: If the token request contains a client_id parameter as per [RFC6749],
        // the Authorization Server MUST verify that the value of this parameter is the same as
        // the client_id value in the subclaim of the Client Attestation.
        String requestClientId = (String) additionalParams.get(OAuth2ParameterNames.CLIENT_ID);
        if (requestClientId != null && !requestClientId.equals(resolvedClientId)) {
            throw invalidClient("client_id mismatch");
        }

        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(resolvedClientId);
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        boolean additionalSecuritySignal = Optional.ofNullable(additionalParams.get(EulerOAuth2ParameterNames.ADDITIONAL_SECURITY_SIGNAL))
                .map(Boolean.class::cast)
                .orElse(false);

        if (!registeredClient.getClientAuthenticationMethods()
                .contains(clientAuthentication.getClientAuthenticationMethod())
                && !additionalSecuritySignal) {
            throw invalidClient("authentication_method");
        }

        // Return authenticated token with the verified attestation registration as credentials for downstream extraction
        return new OAuth2ClientAuthenticationToken(registeredClient,
                EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH, appRegistration);
    }

    @Override
    public boolean supports(@Nonnull Class<?> authentication) {
        return OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication);
    }


    private static OAuth2AuthenticationException invalidClient(String parameterName) {
        OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT,
                "Client authentication failed: " + parameterName, null);
        return new OAuth2AuthenticationException(error);
    }

    /**
     * Require that a verified App Attest registration is bound to an OAuth2 client.
     * <p>
     * Only a STATIC app has an app-level client, pre-provisioned when the app is saved, so
     * its {@code client_id} is bound at attestation time. A DYNAMIC app has no shared
     * client: its per-key {@code client_id} is minted and bound back during dynamic client
     * registration, so until then nothing can be resolved. The same holds for an app that is
     * not OAuth2-enabled.
     * <p>
     * Reported as {@code unauthorized_client} rather than an attestation failure, because the
     * App Attest proof itself was valid; the app is simply not allowed to authenticate this
     * way yet. The device KEY registration is deliberately kept: it was fully verified and is
     * exactly what the client needs for its next step, so rejecting the request does not force
     * it to re-attest.
     */
    private static void requireBoundClientId(AppAttestAttestationRegistration registration) {
        if (StringUtils.hasText(registration.getClientId())) {
            return;
        }
        throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
                "No OAuth2 client is bound to this app; the device KEY is registered, "
                        + "complete dynamic client registration first", null));
    }

    private static OAuth2AuthenticationException invalidClientAttestation(String parameterName) {
        OAuth2Error error = new OAuth2Error(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION,
                "Client attestation failed: " + parameterName, null);
        return new OAuth2AuthenticationException(error);
    }
}
