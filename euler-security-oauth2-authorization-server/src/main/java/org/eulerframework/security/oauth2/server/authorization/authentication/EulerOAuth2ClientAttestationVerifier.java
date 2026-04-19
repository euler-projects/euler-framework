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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.annotation.Nullable;
import org.eulerframework.security.oauth2.server.authorization.web.EulerOAuth2AttestationBasedClientAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.util.Assert;

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.NonceService;
import org.eulerframework.security.authentication.appattest.DeviceAppAttestationRegistration;
import org.eulerframework.security.authentication.appattest.DeviceAppAttestationRegistrationService;
import org.eulerframework.security.oauth2.core.EulerOAuth2ErrorCodes;

/**
 * Unified verifier for Client Attestation and PoP JWTs as defined in
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-08.html">
 * draft-ietf-oauth-attestation-based-client-auth-08</a>.
 * <p>
 * This class merges the responsibilities of the former {@code PopJwtVerifier} (PoP JWT
 * signature and claims verification) and the former {@code ClientAttestationVerifier}
 * interface (Client Attestation JWT verification). It provides two verification entry points:
 * <ul>
 *   <li>{@link #verify(String, String)} — standard draft flow (Section 6.2): verifies the
 *       Client Attestation JWT, extracts the {@code cnf} public key, then verifies the PoP JWT.
 *       <b>Attestation JWT verification is not yet implemented</b>; the method currently
 *       degrades to kid-based lookup.</li>
 *   <li>{@link #verify(String)} — kid-based lookup mode: the PoP JWT header must carry a
 *       {@code kid}, which is used to look up the public key from
 *       {@link DeviceAppAttestationRegistrationService}.</li>
 * </ul>
 * <p>
 * Both methods return a {@link PopVerificationResult} containing the resolved {@code keyId},
 * {@code clientId}, and {@link DeviceAppAttestationRegistration}.
 *
 * @see EulerOAuth2ClientAttestationAuthenticationProvider
 * @see EulerOAuth2AttestationBasedClientAuthenticationFilter
 */
public final class EulerOAuth2ClientAttestationVerifier {

    private final Logger logger = LoggerFactory.getLogger(EulerOAuth2ClientAttestationVerifier.class);

    static final Duration POP_JWT_MAX_AGE = Duration.ofMinutes(5);
    static final Duration POP_JWT_CLOCK_SKEW = Duration.ofSeconds(30);
    static final String POP_JWT_TYPE = "oauth-client-attestation-pop+jwt";

    private final ChallengeService challengeService;
    private final NonceService nonceService;

    private DeviceAppAttestationRegistrationService deviceAppAttestationRegistrationService;


    public EulerOAuth2ClientAttestationVerifier(ChallengeService challengeService, NonceService nonceService) {
        Assert.notNull(challengeService, "challengeService must not be null");
        Assert.notNull(nonceService, "nonceService must not be null");
        this.challengeService = challengeService;
        this.nonceService = nonceService;
    }

    public void setDeviceAttestRegistrationService(DeviceAppAttestationRegistrationService deviceAppAttestationRegistrationService) {
        this.deviceAppAttestationRegistrationService = deviceAppAttestationRegistrationService;
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
     * {@link DeviceAppAttestationRegistration} and its public key from
     * {@link DeviceAppAttestationRegistrationService}.
     *
     * @param popJwt the PoP JWT (Section 5.2)
     * @return the verification result containing keyId, clientId and registration
     * @throws OAuth2AuthenticationException if verification fails
     */
    public PopVerificationResult verify(String popJwt) {
        Assert.hasText(popJwt, "popJwt must not be empty");

        if (this.deviceAppAttestationRegistrationService == null) {
            throw attestationError(
                    "Single PoP JWT verification mode requires App Attest registration service; "
                            + "enable euler.security.app-attest or provide both OAuth-Client-Attestation and OAuth-Client-Attestation-PoP headers");
        }

        try {
            SignedJWT signedJWT = SignedJWT.parse(popJwt);
            JWSHeader header = signedJWT.getHeader();
            String kid = header.getKeyID();

            if (kid == null) {
                throw attestationError("PoP JWT missing kid in header");
            }

            DeviceAppAttestationRegistration registration = this.deviceAppAttestationRegistrationService.findByKeyId(kid);
            if (registration == null) {
                throw attestationError("Unknown key_id: " + kid);
            }

            PublicKey publicKey = registration.getPublicKey();
            verifyPopJwt(signedJWT, publicKey);

            return new PopVerificationResult(kid, registration.getTeamId() + "." + registration.getBundleId(), registration);

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
     * Result of Client Attestation PoP verification, containing the resolved key ID,
     * client ID, and the associated {@link DeviceAppAttestationRegistration}.
     *
     * @param keyId        the verified key ID from the PoP JWT header
     * @param clientId     the client ID associated with the key, or {@code null} if not bound
     * @param registration the {@link DeviceAppAttestationRegistration} associated with the key,
     *                     or {@code null} if resolved from attestation JWT cnf (future)
     */
    public record PopVerificationResult(
            String keyId,
            @Nullable String clientId,
            @Nullable DeviceAppAttestationRegistration registration) {
    }
}
