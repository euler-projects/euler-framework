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

import java.security.PublicKey;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.NonceService;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistrationService;
import org.eulerframework.security.authentication.appattest.apple.AppleAppAttestValidationService;
import org.eulerframework.security.oauth2.core.EulerOAuth2ClientAttestationType;
import org.eulerframework.security.oauth2.core.EulerOAuth2ErrorCodes;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2HeaderNames;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;

/**
 * Unified verifier for Client Attestation and PoP JWTs as defined in
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-08.html">
 * draft-ietf-oauth-attestation-based-client-auth-08</a>.
 * <p>
 * {@link #verify(Map)} is the entry point callers use: it dispatches on the collected
 * {@code OAuth-Client-Attestation-Type} and returns the resolved {@code client_id} with the verified
 * registration. The two {@code verify} overloads implement the JWT variant and return a
 * {@link PopVerificationResult}.
 *
 * @see EulerOAuth2ClientAttestationAuthenticationProvider
 * @see org.eulerframework.security.oauth2.server.authorization.web.authentication.EulerOAuth2ClientAttestationAuthenticationSuccessHandler
 */
public final class EulerOAuth2ClientAttestationVerifier {

    private final Logger logger = LoggerFactory.getLogger(EulerOAuth2ClientAttestationVerifier.class);

    static final Duration POP_JWT_MAX_AGE = Duration.ofMinutes(5);
    static final Duration POP_JWT_CLOCK_SKEW = Duration.ofSeconds(30);
    static final String POP_JWT_TYPE = "oauth-client-attestation-pop+jwt";

    private final ChallengeService challengeService;
    private final NonceService nonceService;

    private AppAttestAttestationRegistrationService appAttestAttestationRegistrationService;

    private AppleAppAttestValidationService appleAppAttestValidationService;


    public EulerOAuth2ClientAttestationVerifier(ChallengeService challengeService, NonceService nonceService) {
        Assert.notNull(challengeService, "challengeService must not be null");
        Assert.notNull(nonceService, "nonceService must not be null");
        this.challengeService = challengeService;
        this.nonceService = nonceService;
    }

    public void setDeviceAttestRegistrationService(AppAttestAttestationRegistrationService appAttestAttestationRegistrationService) {
        this.appAttestAttestationRegistrationService = appAttestAttestationRegistrationService;
    }

    public void setAppleAppAttestValidationService(AppleAppAttestValidationService appleAppAttestValidationService) {
        this.appleAppAttestValidationService = appleAppAttestValidationService;
    }

    /**
     * Verify the client attestation carried in {@code collectedParams} and resolve the client it
     * authenticates, without touching any {@code RegisteredClientRepository}.
     * <p>
     * Dispatch is by the {@code OAuth-Client-Attestation-Type} the converter collected:
     * <ul>
     *   <li>{@link EulerOAuth2ClientAttestationType#JWT} &mdash; verify the PoP JWT (and, when
     *       present, the Client Attestation JWT) via the kid-based flow below.</li>
     *   <li>{@link EulerOAuth2ClientAttestationType#APPLE_APP_ATTEST} &mdash; consume the one-time
     *       challenge exactly once, then validate the {@code attestation} and/or {@code assertion}.
     *       The two are not mutually exclusive; see {@link EulerOAuth2ParameterNames#ATTESTATION}
     *       for the combined-request semantics. Only available when Apple App Attest is enabled.</li>
     * </ul>
     * The returned {@code clientId} is always non-null: an attestation that verifies but resolves
     * no bound client is rejected here rather than handed back ambiguous, so callers can look the
     * client up directly.
     *
     * @param collectedParams the attestation data collected by
     *                        {@link org.eulerframework.security.oauth2.server.authorization.web.authentication.EulerOAuth2ClientAttestationAuthenticationConverter}
     * @return the resolved {@code client_id} and the verified registration
     * @throws OAuth2AuthenticationException if verification fails or no client is bound
     */
    public ClientAttestationVerification verify(Map<String, Object> collectedParams) {
        EulerOAuth2ClientAttestationType clientAttestationType = (EulerOAuth2ClientAttestationType) collectedParams
                .get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE);

        final String resolvedClientId;
        final AppAttestAttestationRegistration registration;

        if (EulerOAuth2ClientAttestationType.JWT.equals(clientAttestationType)) {
            String attestationJwt = (String) collectedParams.get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION);
            String attestationPopJwt = (String) collectedParams.get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_POP);
            if (attestationPopJwt == null) {
                throw invalidClientAttestation(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_POP);
            }
            PopVerificationResult result = attestationJwt == null
                    ? verify(attestationPopJwt)
                    : verify(attestationJwt, attestationPopJwt);
            registration = result.registration();
            resolvedClientId = result.clientId();
        } else if (EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST.equals(clientAttestationType)) {
            if (this.appleAppAttestValidationService == null) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION,
                                "APP_ATTEST attestation type is not supported; "
                                        + "enable euler.security.authentication.app-attest to use this attestation type", null));
            }

            // The converter normalizes both carriages onto the canonical header keys, so this
            // verifier is transport-agnostic; only the deprecated attestation has no header analog.
            String challenge = (String) collectedParams.get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE);
            if (!StringUtils.hasText(challenge)) {
                throw invalidClientAttestation(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE);
            }

            String attestation = (String) collectedParams.get(EulerOAuth2ParameterNames.ATTESTATION);
            String assertion = (String) collectedParams.get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION);
            if (!StringUtils.hasText(attestation) && !StringUtils.hasText(assertion)) {
                // Reject before consuming, so a malformed request does not burn an otherwise valid
                // one-time challenge.
                throw invalidClientAttestation(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION);
            }

            // Consume the one-time challenge exactly once, before any verification. Consumption is
            // decoupled from verification (both only use the challenge as nonce input), so a single
            // challenge may legitimately back both an attestation and an assertion derived from it.
            if (!this.challengeService.consumeChallenge(challenge)) {
                throw invalidClientAttestation(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE);
            }

            if (StringUtils.hasText(attestation)) {
                // Compatibility path; see EulerOAuth2ParameterNames#ATTESTATION. Register the device
                // KEY (idempotent); the key ID is derived from the attestation's credentialId.
                AppAttestAttestationRegistration registered =
                        this.appleAppAttestValidationService.validateAttestation(attestation, challenge);

                // Fail fast, before verifying any accompanying assertion.
                requireBoundClientId(registered);

                if (StringUtils.hasText(assertion)) {
                    registration = this.appleAppAttestValidationService.validateAssertion(
                            registered.getKeyId(), assertion, challenge);
                } else {
                    registration = registered;
                }
            } else {
                // Assertion-only fast path; an assertion's authenticator data carries no credentialId,
                // so the key ID must be supplied to locate the registered device.
                String keyId = (String) collectedParams.get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID);
                if (!StringUtils.hasText(keyId)) {
                    throw invalidClientAttestation(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID);
                }
                registration = this.appleAppAttestValidationService.validateAssertion(keyId, assertion, challenge);
                requireBoundClientId(registration);
            }

            resolvedClientId = registration.getClientId();
        } else {
            throw invalidClientAttestation(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE);
        }

        if (resolvedClientId == null) {
            throw invalidClientAttestation(OAuth2ParameterNames.CLIENT_ID);
        }

        // Draft Section 6.3: if the request carries a client_id parameter, the Authorization Server
        // MUST verify it equals the client_id resolved from the Client Attestation.
        String requestClientId = (String) collectedParams.get(OAuth2ParameterNames.CLIENT_ID);
        if (requestClientId != null && !requestClientId.equals(resolvedClientId)) {
            throw invalidClient("client_id mismatch");
        }

        return new ClientAttestationVerification(resolvedClientId, registration);
    }

    /**
     * Verify a Client Attestation JWT together with a PoP JWT (standard draft flow,
     * Section 6.2).
     * <p>
     * <b>Note:</b> Client Attestation JWT verification is not yet implemented.
     * This method always throws an error.
     *
     * @param attestationJwt the Client Attestation JWT (Section 5.1)
     * @param popJwt         the PoP JWT (Section 5.2)
     * @return never returns normally
     * @throws OAuth2AuthenticationException always, indicating the feature is not yet implemented
     */
    public PopVerificationResult verify(String attestationJwt, String popJwt) {
        Assert.hasText(attestationJwt, "attestationJwt must not be empty");
        Assert.hasText(popJwt, "popJwt must not be empty");

        // TODO: Implement Client Attestation JWT verification (Section 5.1)
        //       - Verify signature using the Client Attester's public key
        //       - Extract cnf claim to obtain the client instance public key
        //       - Use that public key to verify the PoP JWT instead of kid lookup
        throw attestationError("Client Attestation JWT verification is not yet implemented");
    }

    /**
     * Verify a PoP JWT using kid-based key lookup.
     * <p>
     * The PoP JWT header must carry a {@code kid} which is used to look up the
     * {@link AppAttestAttestationRegistration} and its public key from
     * {@link AppAttestAttestationRegistrationService}.
     *
     * @param popJwt the PoP JWT (Section 5.2)
     * @return the verification result containing keyId, clientId and registration
     * @throws OAuth2AuthenticationException if verification fails
     */
    public PopVerificationResult verify(String popJwt) {
        Assert.hasText(popJwt, "popJwt must not be empty");

        if (this.appAttestAttestationRegistrationService == null) {
            throw attestationError(
                    "Single PoP JWT verification mode requires App Attest registration service; "
                            + "enable euler.security.authentication.app-attest or provide both OAuth-Client-Attestation and OAuth-Client-Attestation-PoP headers");
        }

        try {
            SignedJWT signedJWT = SignedJWT.parse(popJwt);
            JWSHeader header = signedJWT.getHeader();
            String kid = header.getKeyID();

            if (kid == null) {
                throw attestationError("PoP JWT missing kid in header");
            }

            AppAttestAttestationRegistration registration = this.appAttestAttestationRegistrationService.findByKeyId(kid);
            if (registration == null) {
                throw attestationError("Unknown kid: " + kid);
            }

            PublicKey publicKey = registration.getPublicKey();
            verifyPopJwt(signedJWT, publicKey);

            return new PopVerificationResult(kid, registration.getClientId(), registration);

        } catch (ParseException e) {
            throw attestationError("Failed to parse PoP JWT: " + e.getMessage());
        }
    }

    // ========== Internal PoP JWT verification ==========

    private void verifyPopJwt(SignedJWT signedJWT, PublicKey publicKey) {
        try {
            JWSHeader header = signedJWT.getHeader();

            // Verify signature
            JWSVerifier verifier = new DefaultJWSVerifierFactory()
                    .createJWSVerifier(header, publicKey);
            if (!signedJWT.verify(verifier)) {
                throw attestationError("PoP JWT signature verification failed");
            }

            // Verify typ (REQUIRED per draft)
            JOSEObjectType typ = header.getType();
            if (typ == null || !POP_JWT_TYPE.equals(typ.getType())) {
                throw attestationError("PoP JWT typ must be '" + POP_JWT_TYPE + "'");
            }

            // Verify claims
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            // aud (REQUIRED) — must contain AS issuer
            AuthorizationServerContext asContext = AuthorizationServerContextHolder.getContext();
            if (asContext != null && asContext.getIssuer() != null) {
                List<String> audience = claims.getAudience();
                if (audience == null || !audience.contains(asContext.getIssuer())) {
                    throw attestationError(
                            "PoP JWT aud does not match authorization server issuer");
                }
            }

            // iat (REQUIRED) — must be within acceptable time window
            Date iat = claims.getIssueTime();
            if (iat == null) {
                throw attestationError("PoP JWT missing iat claim");
            }
            Instant now = Instant.now();
            Instant issuedAt = iat.toInstant();
            if (issuedAt.isAfter(now.plus(POP_JWT_CLOCK_SKEW))
                    || issuedAt.isBefore(now.minus(POP_JWT_MAX_AGE))) {
                throw attestationError("PoP JWT iat is outside acceptable time window");
            }

            // challenge claim
            String challenge = (String) claims.getClaim("challenge");
            if (challenge == null || !this.challengeService.consumeChallenge(challenge)) {
                throw attestationError("PoP JWT challenge is invalid or expired");
            }

            // jti replay detection (Section 12.1)
            String jti = claims.getJWTID();
            if (jti == null || jti.isBlank()) {
                throw attestationError("PoP JWT missing jti claim");
            }
            if (!this.nonceService.recordIfAbsent(jti, POP_JWT_MAX_AGE)) {
                throw attestationError("PoP JWT replay detected (duplicate jti)");
            }

        } catch (ParseException | JOSEException e) {
            throw attestationError("Failed to verify PoP JWT: " + e.getMessage());
        }
    }

    private static OAuth2AuthenticationException attestationError(String description) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION, description, null));
    }

    /**
     * Require that a verified App Attest registration is bound to an OAuth2 client.
     * <p>
     * Only a STATIC app has an app-level client, pre-provisioned when the app is saved, so its
     * {@code client_id} is bound at attestation time. A DYNAMIC app has no shared client: its
     * per-key {@code client_id} is minted and bound back during dynamic client registration, so
     * until then nothing can be resolved. The same holds for an app that is not OAuth2-enabled.
     * <p>
     * Reported as {@code unauthorized_client} rather than an attestation failure, because the App
     * Attest proof itself was valid; the app is simply not allowed to authenticate this way yet.
     * The device KEY registration is deliberately kept: it was fully verified and is exactly what
     * the client needs for its next step, so rejecting the request does not force it to re-attest.
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
        return new OAuth2AuthenticationException(new OAuth2Error(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION,
                "Client attestation failed: " + parameterName, null));
    }

    private static OAuth2AuthenticationException invalidClient(String parameterName) {
        return new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT,
                "Client authentication failed: " + parameterName, null));
    }

    /**
     * Result of Client Attestation PoP verification, containing the resolved key ID,
     * client ID, and the associated {@link AppAttestAttestationRegistration}.
     *
     * @param keyId        the verified key ID from the PoP JWT header
     * @param clientId     the client ID associated with the key, or {@code null} if not bound
     * @param registration the {@link AppAttestAttestationRegistration} associated with the key,
     *                     or {@code null} if resolved from attestation JWT cnf (future)
     */
    public record PopVerificationResult(
            String keyId,
            @Nullable String clientId,
            @Nullable AppAttestAttestationRegistration registration) {
    }

    /**
     * Result of a successful client attestation verification via {@link #verify(Map)}: the client
     * the attestation authenticates and the verified device registration behind it.
     *
     * @param clientId     the resolved, non-null {@code client_id} bound to the attestation
     * @param registration the verified {@link AppAttestAttestationRegistration}
     */
    public record ClientAttestationVerification(
            String clientId,
            AppAttestAttestationRegistration registration) {
    }
}
