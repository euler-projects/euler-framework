/*
 * Copyright 2013-2024 the original author or authors.
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
package org.eulerframework.security.oauth2.server.authorization.web;

import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.security.authentication.apple.AppAttestRegistration;
import org.eulerframework.security.authentication.apple.AppAttestRegistrationService;
import org.eulerframework.security.authentication.apple.AppleAppAttestValidationService;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerOAuth2ErrorCodes;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;
import org.eulerframework.security.oauth2.server.authorization.authentication.ClientAttestationVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Post-authentication filter that handles Client Attestation on token endpoint requests,
 * as defined in
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-08.html">
 * draft-ietf-oauth-attestation-based-client-auth-08</a>.
 * <p>
 * This filter runs <b>after</b> {@code OAuth2ClientAuthenticationFilter} and handles two cases:
 * <ul>
 *   <li><b>{@code attest_jwt_client_auth} clients</b>: Already fully authenticated by
 *       {@link org.eulerframework.security.oauth2.server.authorization.authentication.ClientAttestationAuthenticationProvider
 *       ClientAttestationAuthenticationProvider}. This filter simply extracts the verified
 *       {@code key_id} from the authentication token and sets it as a request attribute
 *       for downstream components.</li>
 *   <li><b>Standard clients with attestation headers</b> (Scenario A): The client was
 *       authenticated via standard methods (e.g., {@code client_secret_basic}, PKCE).
 *       This filter verifies the attestation data as an additional security signal.</li>
 * </ul>
 * <p>
 * For Scenario A, PoP verification is dispatched by the
 * {@code OAuth-Client-Attestation-PoP-Type} header:
 * <ul>
 *   <li>{@code jwt} (default) — standard PoP JWT as defined in Section 5.2 of the draft.</li>
 *   <li>{@code App-Attest} — Apple App Attest Assertion used as PoP, with parameters
 *       ({@code key_id}, {@code assertion_data}, {@code challenge}) in the request body.</li>
 * </ul>
 *
 * @see org.eulerframework.security.oauth2.server.authorization.authentication.ClientAttestationAuthenticationProvider
 * @see ClientAttestationVerifier
 * @see EulerOAuth2ParameterNames
 */
public class ClientAttestationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ClientAttestationFilter.class);

    /**
     * Request attribute name for the verified key ID.
     * Set by this filter after successful attestation verification; read by downstream
     * converters (e.g., {@code OAuth2AppleAppAttestAssertionAuthenticationConverter}).
     */
    public static final String ATTESTATION_VERIFIED_KEY_ID_ATTRIBUTE = "attestation.verified.key_id";

    // --- Required dependencies ---
    private final AppAttestRegistrationService appAttestRegistrationService;
    private final AppleAppAttestValidationService appleAppAttestValidationService;
    private final RequestMatcher tokenEndpointMatcher;

    // --- Optional dependencies ---
    @Nullable
    private ClientAttestationVerifier clientAttestationVerifier;

    public ClientAttestationFilter(
            AppAttestRegistrationService appAttestRegistrationService,
            AppleAppAttestValidationService appleAppAttestValidationService,
            RequestMatcher tokenEndpointMatcher) {
        Assert.notNull(appAttestRegistrationService, "appAttestRegistrationService must not be null");
        Assert.notNull(appleAppAttestValidationService, "appleAppAttestValidationService must not be null");
        Assert.notNull(tokenEndpointMatcher, "tokenEndpointMatcher must not be null");
        this.appAttestRegistrationService = appAttestRegistrationService;
        this.appleAppAttestValidationService = appleAppAttestValidationService;
        this.tokenEndpointMatcher = tokenEndpointMatcher;
    }

    public void setClientAttestationVerifier(@Nullable ClientAttestationVerifier clientAttestationVerifier) {
        this.clientAttestationVerifier = clientAttestationVerifier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. Only apply to token endpoint requests
        if (!this.tokenEndpointMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Get current authentication from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof OAuth2ClientAuthenticationToken clientAuth) || !clientAuth.isAuthenticated()) {
            // No client authentication → pass through
            filterChain.doFilter(request, response);
            return;
        }

        // 3. If already authenticated via attest_jwt_client_auth (by Converter+Provider),
        //    extract keyId from additional parameters and set as request attribute
        if (EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH
                .equals(clientAuth.getClientAuthenticationMethod())) {
            String keyId = clientAuth.getCredentials() instanceof String k ? k : null;
            if (keyId != null) {
                request.setAttribute(ATTESTATION_VERIFIED_KEY_ID_ATTRIBUTE, keyId);
            }
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Scenario A: Standard client auth + Attestation enhancement
        String attestationJwt = request.getHeader(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION);
        String popData = request.getHeader(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP);
        String popTypeHeader = request.getHeader(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP_TYPE);

        boolean hasAttestation = attestationJwt != null || popData != null || popTypeHeader != null;
        if (!hasAttestation) {
            // No attestation data → pass through
            filterChain.doFilter(request, response);
            return;
        }

        String popType = popTypeHeader != null ? popTypeHeader : EulerOAuth2ParameterNames.POP_TYPE_JWT;

        try {
            RegisteredClient registeredClient = clientAuth.getRegisteredClient();
            AttestationVerificationResult result = verifyAttestation(
                    request, attestationJwt, popData, popType, registeredClient);

            // Set request attribute for downstream components (e.g., Grant Type Converter)
            request.setAttribute(ATTESTATION_VERIFIED_KEY_ID_ATTRIBUTE, result.resolvedKeyId());

        } catch (OAuth2AuthenticationException ex) {
            sendAttestationError(response, ex.getError());
            return;
        } catch (AuthenticationException ex) {
            OAuth2Error error = new OAuth2Error(
                    EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION, ex.getMessage(), null);
            sendAttestationError(response, error);
            return;
        }

        filterChain.doFilter(request, response);
    }

    // ========== Attestation Verification ==========

    private AttestationVerificationResult verifyAttestation(
            HttpServletRequest request,
            @Nullable String attestationJwt,
            @Nullable String popData,
            String popType,
            @Nullable RegisteredClient registeredClient) {

        AppAttestRegistration registration = null;
        String resolvedClientId = null;
        String resolvedKeyId = null;

        // === 1. PoP verification (dispatched by popType, mutually exclusive) ===
        switch (popType) {

            case EulerOAuth2ParameterNames.POP_TYPE_JWT -> {
                // Standard PoP JWT (draft Section 5.2) — delegated to ClientAttestationVerifier
                if (popData == null) {
                    throw attestationError("PoP-Type=jwt but missing "
                            + EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP + " header");
                }
                if (this.clientAttestationVerifier == null) {
                    throw attestationError("ClientAttestationVerifier is not configured");
                }

                ClientAttestationVerifier.PopVerificationResult result;
                if (attestationJwt != null) {
                    result = this.clientAttestationVerifier.verify(attestationJwt, popData);
                } else {
                    result = this.clientAttestationVerifier.verify(popData);
                }
                resolvedKeyId = result.keyId();
                registration = result.registration();
                if (resolvedClientId == null) {
                    resolvedClientId = result.clientId();
                }
            }

            case EulerOAuth2ParameterNames.POP_TYPE_APP_ATTEST -> {
                // Apple App Attest Assertion used as PoP
                String keyId = request.getParameter(EulerOAuth2ParameterNames.KEY_ID);
                String assertion = request.getParameter(EulerOAuth2ParameterNames.ASSERTION);
                String challenge = request.getParameter(EulerOAuth2ParameterNames.CHALLENGE);

                if (!StringUtils.hasText(keyId) || !StringUtils.hasText(assertion)
                        || !StringUtils.hasText(challenge)) {
                    throw attestationError("App-Attest PoP requires "
                            + EulerOAuth2ParameterNames.KEY_ID + ", "
                            + EulerOAuth2ParameterNames.ASSERTION + ", and "
                            + EulerOAuth2ParameterNames.CHALLENGE + " parameters");
                }

                resolvedKeyId = keyId;
                registration = this.appAttestRegistrationService.findByKeyId(keyId);
                if (registration == null) {
                    throw attestationError("Unknown key_id: " + keyId);
                }
                if (resolvedClientId == null) {
                    resolvedClientId = registration.getClientId();
                }

                // Validate assertion via AppleAppAttestValidationService
                // (signature verification + signCount check + update)
                this.appleAppAttestValidationService.validateAssertion(keyId, assertion, challenge);
            }

            default -> throw attestationError("Unsupported PoP-Type: " + popType);
        }

        // === 3. client_id consistency check ===
        if (registeredClient != null && resolvedClientId != null
                && !resolvedClientId.equals(registeredClient.getClientId())) {
            throw attestationError("Attestation client_id mismatch with authenticated client");
        }

        return new AttestationVerificationResult(resolvedClientId, registration, resolvedKeyId);
    }

    // ========== Helpers ==========

    private static OAuth2AuthenticationException attestationError(String description) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION, description, null));
    }

    private static void sendAttestationError(HttpServletResponse response, OAuth2Error error)
            throws IOException {
        int status = OAuth2ErrorCodes.INVALID_CLIENT.equals(error.getErrorCode())
                ? HttpServletResponse.SC_UNAUTHORIZED
                : HttpServletResponse.SC_BAD_REQUEST;
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.addHeader("Cache-Control", "no-store");
        response.addHeader("Pragma", "no-cache");

        StringBuilder json = new StringBuilder();
        json.append("{\"error\":\"").append(escapeJson(error.getErrorCode())).append("\"");
        if (error.getDescription() != null) {
            json.append(",\"error_description\":\"")
                    .append(escapeJson(error.getDescription())).append("\"");
        }
        json.append("}");

        response.getWriter().write(json.toString());
        response.flushBuffer();
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record AttestationVerificationResult(
            @Nullable String resolvedClientId,
            @Nullable AppAttestRegistration registration,
            @Nullable String resolvedKeyId) {
    }
}
