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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.util.Assert;

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.ClientAttestationVerifier;
import org.eulerframework.security.authentication.NonceService;
import org.eulerframework.security.authentication.apple.AppAttestRegistration;
import org.eulerframework.security.authentication.apple.AppAttestRegistrationService;
import org.eulerframework.security.authentication.apple.AppleAppAttestValidationService;
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
 * It works in tandem with {@link org.eulerframework.security.oauth2.server.authorization.web.authentication.ClientAttestationAuthenticationConverter
 * ClientAttestationAuthenticationConverter}, which extracts the attestation data from
 * the request and creates an unauthenticated {@link OAuth2ClientAuthenticationToken}.
 * <p>
 * The provider performs the following verification steps:
 * <ol>
 *   <li>Resolves the {@link RegisteredClient} and verifies it supports
 *       {@code attest_jwt_client_auth}.</li>
 *   <li>Optionally verifies the Client Attestation JWT via
 *       {@link ClientAttestationVerifier}.</li>
 *   <li>Verifies the Proof-of-Possession (PoP) data — either a standard PoP JWT
 *       (Section 5.2) or an Apple App Attest Assertion.</li>
 * </ol>
 * <p>
 * After successful authentication, the verified {@code key_id} is preserved in the
 * authenticated token's {@code additionalParameters} for downstream components.
 *
 * @see org.eulerframework.security.oauth2.server.authorization.web.authentication.ClientAttestationAuthenticationConverter
 * @see EulerClientAuthenticationMethod#ATTEST_JWT_CLIENT_AUTH
 */
public final class ClientAttestationAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(ClientAttestationAuthenticationProvider.class);

    private static final Duration POP_JWT_MAX_AGE = Duration.ofMinutes(5);
    private static final Duration POP_JWT_CLOCK_SKEW = Duration.ofSeconds(30);
    private static final String POP_JWT_TYPE = "oauth-client-attestation-pop+jwt";

    private final RegisteredClientRepository registeredClientRepository;
    private final AppAttestRegistrationService appAttestRegistrationService;
    private final AppleAppAttestValidationService appleAppAttestValidationService;

    @Nullable
    private ClientAttestationVerifier clientAttestationVerifier;
    @Nullable
    private ChallengeService challengeService;
    @Nullable
    private NonceService nonceService;

    public ClientAttestationAuthenticationProvider(
            RegisteredClientRepository registeredClientRepository,
            AppAttestRegistrationService appAttestRegistrationService,
            AppleAppAttestValidationService appleAppAttestValidationService) {
        Assert.notNull(registeredClientRepository, "registeredClientRepository must not be null");
        Assert.notNull(appAttestRegistrationService, "appAttestRegistrationService must not be null");
        Assert.notNull(appleAppAttestValidationService, "appleAppAttestValidationService must not be null");
        this.registeredClientRepository = registeredClientRepository;
        this.appAttestRegistrationService = appAttestRegistrationService;
        this.appleAppAttestValidationService = appleAppAttestValidationService;
    }

    public void setClientAttestationVerifier(@Nullable ClientAttestationVerifier clientAttestationVerifier) {
        this.clientAttestationVerifier = clientAttestationVerifier;
    }

    public void setChallengeService(@Nullable ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    public void setNonceService(@Nullable NonceService nonceService) {
        this.nonceService = nonceService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2ClientAuthenticationToken clientAuth = (OAuth2ClientAuthenticationToken) authentication;

        if (!EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH
                .equals(clientAuth.getClientAuthenticationMethod())) {
            return null;
        }

        String clientId = clientAuth.getPrincipal().toString();
        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        if (!registeredClient.getClientAuthenticationMethods()
                .contains(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_CLIENT,
                    "Client does not support 'attest_jwt_client_auth' authentication method", null));
        }

        Map<String, Object> additionalParams = clientAuth.getAdditionalParameters();
        String popType = (String) additionalParams.get(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP_TYPE);
        String popData = (String) additionalParams.get(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP);
        String attestationJwt = (String) additionalParams.get(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION);
        String keyId = (String) additionalParams.get(EulerOAuth2ParameterNames.KEY_ID);

        // === 1. Client Attestation JWT verification (optional) ===
        PublicKey publicKey = null;
        if (attestationJwt != null) {
            if (this.clientAttestationVerifier != null) {
                publicKey = this.clientAttestationVerifier.verifyClientAttestation(attestationJwt);
            } else {
                logger.warn("Received Client Attestation JWT but no ClientAttestationVerifier configured; ignoring");
            }
        }

        // === 2. PoP verification (dispatched by popType) ===
        switch (popType) {
            case EulerOAuth2ParameterNames.POP_TYPE_JWT ->
                    verifyPopJwt(popData, keyId, publicKey);
            case EulerOAuth2ParameterNames.POP_TYPE_APP_ATTEST -> {
                String assertion = (String) additionalParams.get(EulerOAuth2ParameterNames.ASSERTION);
                String challenge = (String) additionalParams.get(EulerOAuth2ParameterNames.CHALLENGE);
                this.appleAppAttestValidationService.validateAssertion(keyId, assertion, challenge);
            }
            default -> throw attestationError("Unsupported PoP-Type: " + popType);
        }

        // Return authenticated token with keyId as credentials for downstream extraction
        return new OAuth2ClientAuthenticationToken(registeredClient,
                EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH, keyId);
    }

    private void verifyPopJwt(@Nullable String popData, String keyId, @Nullable PublicKey publicKey) {
        if (popData == null) {
            throw attestationError("PoP-Type=jwt but missing "
                    + EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP + " header");
        }

        try {
            SignedJWT signedJWT = SignedJWT.parse(popData);
            JWSHeader header = signedJWT.getHeader();

            // If no public key from attestation JWT, look up by kid
            if (publicKey == null) {
                AppAttestRegistration registration = this.appAttestRegistrationService.findByKeyId(keyId);
                if (registration == null) {
                    throw attestationError("Unknown key_id: " + keyId);
                }
                publicKey = registration.getPublicKey();
            }

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

            // challenge claim (optional; verified via ChallengeService if present)
            Object challengeClaim = claims.getClaim("challenge");
            if (challengeClaim instanceof String challenge && this.challengeService != null) {
                if (!this.challengeService.consumeChallenge(challenge)) {
                    throw attestationError("PoP JWT challenge is invalid or expired");
                }
            }

            // jti replay detection (Section 12.1)
            String jti = claims.getJWTID();
            if (this.nonceService != null) {
                if (jti == null || jti.isBlank()) {
                    throw attestationError("PoP JWT missing jti claim");
                }
                if (!this.nonceService.recordIfAbsent(jti, POP_JWT_MAX_AGE)) {
                    throw attestationError("PoP JWT replay detected (duplicate jti)");
                }
            }

        } catch (ParseException | JOSEException e) {
            throw attestationError("Failed to parse or verify PoP JWT: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private static OAuth2AuthenticationException attestationError(String description) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION, description, null));
    }
}
