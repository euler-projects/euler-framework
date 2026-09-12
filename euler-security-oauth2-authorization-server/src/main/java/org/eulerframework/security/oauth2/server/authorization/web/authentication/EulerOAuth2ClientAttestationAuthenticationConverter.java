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
package org.eulerframework.security.oauth2.server.authorization.web.authentication;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.eulerframework.security.oauth2.core.EulerOAuth2ClientAttestationType;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;

import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerClientAttestationProof;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2HeaderNames;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;
import org.springframework.util.StringUtils;

/**
 * An {@link AuthenticationConverter} that extracts Client Attestation data from
 * the request and creates an unauthenticated {@link OAuth2ClientAuthenticationToken}
 * for {@code attest_jwt_client_auth} clients.
 * <p>
 * This converter is a pure "data carrier": it detects the presence of attestation
 * headers, collects all raw header values and request parameters into
 * {@code additionalParameters}, and creates a token with a placeholder principal.
 * <b>No JWT parsing, key lookup, or client_id resolution is performed here</b> —
 * all verification and resolution is deferred to
 * {@link EulerOAuth2ClientAttestationAuthenticationProvider}.
 *
 * @see EulerOAuth2ParameterNames
 * @see EulerClientAuthenticationMethod#ATTEST_JWT_CLIENT_AUTH
 */
public final class EulerOAuth2ClientAttestationAuthenticationConverter implements AuthenticationConverter {

    /**
     * Placeholder principal used for the unauthenticated token. The real {@code client_id}
     * is resolved by the provider after attestation verification.
     */
    static final String ATTESTATION_PRINCIPAL_PLACEHOLDER = "__attestation__";

    @Override
    public Authentication convert(HttpServletRequest request) {
        // 1. Check for attestation signal
        String attestationJwt = request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION);
        String attestationPopJwt = request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_POP);
        String attestationType = request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE);

        if (attestationJwt == null && attestationPopJwt == null && attestationType == null) {
            return null;
        }

        EulerOAuth2ClientAttestationType clientAttestationType = attestationType != null
                ? EulerOAuth2ClientAttestationType.parse(attestationType)
                : EulerOAuth2ClientAttestationType.JWT;

        // 2. Collect all raw attestation data — no parsing, no DB lookup
        Map<String, Object> additionalParams = new LinkedHashMap<>();
        additionalParams.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE, clientAttestationType);

        if (EulerOAuth2ClientAttestationType.JWT.equals(clientAttestationType)) {
            // Unlike the draft, we treat OAuth-Client-Attestation as an optional header.
            // As long as the public key has not changed, it can be omitted.
            // However, if OAuth-Client-Attestation is omitted, the PoP JWT header must carry a verified kid.
            copyOptional(attestationJwt, EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION, additionalParams);
            copyRequired(attestationPopJwt, EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_POP, additionalParams);
        } else if (EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST.equals(clientAttestationType)) {
            if (isHeaderCarried(request)) {
                convertAppleAppAttestHeaders(request, additionalParams);
            } else {
                convertAppleAppAttestFormParameters(request, additionalParams);
            }
        }

        // Optional request client_id (for RFC6749 consistency check in Provider)
        copyOptional(request, OAuth2ParameterNames.CLIENT_ID, additionalParams);

        return new OAuth2ClientAuthenticationToken(
                ATTESTATION_PRINCIPAL_PLACEHOLDER,
                EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH,
                null, additionalParams);
    }

    /**
     * Whether the Apple App Attest data is carried in headers rather than in form parameters.
     * <p>
     * The {@code OAuth-Client-Attestation-Assertion} header is the switch. The two carriages are
     * never mixed: a request presenting that header is read entirely from headers, and any App
     * Attest form parameter it may also carry is ignored.
     *
     * @param request the token endpoint request
     * @return {@code true} if the header carriage applies
     */
    public static boolean isHeaderCarried(HttpServletRequest request) {
        return StringUtils.hasText(
                request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION));
    }

    /**
     * Header carriage, the only non-deprecated one: assertion-only, since device registration
     * happens at the dedicated registration endpoint and no attestation header exists.
     */
    private static void convertAppleAppAttestHeaders(HttpServletRequest request,
                                                     Map<String, Object> additionalParams) {
        copyRequiredHeader(request, EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE, additionalParams);
        copyRequiredHeader(request, EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID, additionalParams);
        copyRequiredHeader(request, EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, additionalParams);
    }

    /**
     * Deprecated form-parameter carriage, retained for released clients; see
     * {@link EulerOAuth2ParameterNames#ATTESTATION}.
     * <p>
     * Unlike the header carriage this one may also carry an attestation, so {@code attestation}
     * and {@code assertion} are not mutually exclusive and the three combinations remain valid:
     * attestation only (registers the device KEY and authenticates the client), assertion only
     * (fast path for an already-registered KEY, which is the sole case requiring {@code kid}),
     * or both. Values are stored under the canonical header keys so that downstream components
     * stay transport-agnostic; only {@code attestation} keeps its own key, having no header
     * analog.
     */
    private static void convertAppleAppAttestFormParameters(HttpServletRequest request,
                                                            Map<String, Object> additionalParams) {
        copyRequiredFormParameter(request, EulerOAuth2ParameterNames.CHALLENGE,
                EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE, additionalParams);

        String attestation = request.getParameter(EulerOAuth2ParameterNames.ATTESTATION);
        if (!StringUtils.hasText(attestation)
                && !StringUtils.hasText(request.getParameter(EulerOAuth2ParameterNames.ASSERTION))) {
            throw newError(OAuth2ErrorCodes.INVALID_REQUEST,
                    EulerOAuth2ParameterNames.ATTESTATION + " or " + EulerOAuth2ParameterNames.ASSERTION, null);
        }
        copyOptional(attestation, EulerOAuth2ParameterNames.ATTESTATION, additionalParams);
        copyOptionalFormParameter(request, EulerOAuth2ParameterNames.ASSERTION,
                EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, additionalParams);

        // The kid is derivable from an attestation (its credentialId), so it is required only
        // for the assertion-only request, whose authenticator data carries no credentialId.
        if (!StringUtils.hasText(attestation)) {
            copyRequiredFormParameter(request, EulerOAuth2ParameterNames.KEY_ID,
                    EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID, additionalParams);
        }
    }

    /**
     * Resolve which proof the request presents, mirroring the branch {@link #convert} takes.
     * <p>
     * Both an attestation and a proof of possession resolve to the same verified device
     * registration, so the registration alone cannot tell device registration apart from
     * device verification. Downstream components that may establish persistent state (notably
     * the device-to-user association) need this discriminator, and the token endpoint filter
     * publishes it alongside the verified registration.
     * <p>
     * Where the attestation is looked for depends on the variant: the {@code OAuth-Client-Attestation}
     * header for {@link EulerOAuth2ClientAttestationType#JWT}, the deprecated {@code attestation}
     * form parameter for {@link EulerOAuth2ClientAttestationType#APPLE_APP_ATTEST}. The Apple
     * header carriage has no attestation analog and is therefore always
     * {@link EulerClientAttestationProof#ASSERTION}. A request carrying both an attestation and a
     * proof of possession is reported as {@link EulerClientAttestationProof#ATTESTATION}, because
     * that is the branch the provider acts on first.
     *
     * @param request the token endpoint request
     * @return the proof the server will act on for this request
     */
    public static EulerClientAttestationProof resolveProof(HttpServletRequest request) {
        String attestationType = request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE);
        EulerOAuth2ClientAttestationType clientAttestationType = attestationType != null
                ? EulerOAuth2ClientAttestationType.parse(attestationType)
                : EulerOAuth2ClientAttestationType.JWT;

        boolean attestationPresent;
        if (EulerOAuth2ClientAttestationType.JWT.equals(clientAttestationType)) {
            attestationPresent = StringUtils.hasText(
                    request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION));
        } else {
            attestationPresent = !isHeaderCarried(request)
                    && StringUtils.hasText(request.getParameter(EulerOAuth2ParameterNames.ATTESTATION));
        }

        return attestationPresent
                ? EulerClientAttestationProof.ATTESTATION
                : EulerClientAttestationProof.ASSERTION;
    }

    private static void copyOptional(Object value, String paramName,
                                     Map<String, Object> target) {
        if (value != null) {
            target.put(paramName, value);
        }
    }

    private static void copyOptional(HttpServletRequest request, String paramName,
                                     Map<String, Object> target) {
        String value = request.getParameter(paramName);
        if (value != null) {
            target.put(paramName, value);
        }
    }

    private static void copyRequiredHeader(HttpServletRequest request, String headerName,
                                           Map<String, Object> target) {
        String value = request.getHeader(headerName);
        if (!StringUtils.hasText(value)) {
            throw newError(OAuth2ErrorCodes.INVALID_REQUEST, headerName, null);
        }
        target.put(headerName, value);
    }

    /**
     * Copy a deprecated form parameter under its canonical header key, so that downstream
     * components stay transport-agnostic.
     */
    private static void copyRequiredFormParameter(HttpServletRequest request, String paramName, String targetKey,
                                                  Map<String, Object> target) {
        String value = request.getParameter(paramName);
        if (!StringUtils.hasText(value)) {
            throw newError(OAuth2ErrorCodes.INVALID_REQUEST, paramName, null);
        }
        target.put(targetKey, value);
    }

    private static void copyOptionalFormParameter(HttpServletRequest request, String paramName, String targetKey,
                                                  Map<String, Object> target) {
        String value = request.getParameter(paramName);
        if (StringUtils.hasText(value)) {
            target.put(targetKey, value);
        }
    }

    private static void copyRequired(Object value, String paramName,
                                     Map<String, Object> target) {
        if (value == null) {
            throw newError(OAuth2ErrorCodes.INVALID_REQUEST, paramName, null);
        }
        target.put(paramName, value);
    }

    private static OAuth2AuthenticationException newError(String errorCode, String parameterName, String errorUri) {
        OAuth2Error error = new OAuth2Error(errorCode, "OAuth 2.0 Parameter: " + parameterName, errorUri);
        return new OAuth2AuthenticationException(error);
    }
}
