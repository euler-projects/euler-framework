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
        String attestationJwt = request.getHeader(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION);
        String attestationPopJwt = request.getHeader(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP);
        String attestationType = request.getHeader(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_TYPE);

        if (attestationJwt == null && attestationPopJwt == null && attestationType == null) {
            return null;
        }

        EulerOAuth2ClientAttestationType auth2ClientAttestationType = attestationType != null
                ? EulerOAuth2ClientAttestationType.parse(attestationType)
                : EulerOAuth2ClientAttestationType.JWT;

        // 2. Collect all raw attestation data — no parsing, no DB lookup
        Map<String, Object> additionalParams = new LinkedHashMap<>();
        additionalParams.put(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_TYPE, auth2ClientAttestationType);

        if (EulerOAuth2ClientAttestationType.JWT.equals(auth2ClientAttestationType)) {
            // Unlike the draft, we treat OAuth-Client-Attestation as an optional header.
            // As long as the public key has not changed, it can be omitted.
            // However, if OAuth-Client-Attestation is omitted, the PoP JWT header must carry a verified kid.
            copyOptional(attestationJwt, EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION, additionalParams);
            copyRequired(attestationPopJwt, EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP, additionalParams);
        } else if (EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST.equals(auth2ClientAttestationType)) {
            // Unless the App Attest key is regenerated (causing attestation data to change),
            // attestation data is not required since it was already submitted and verified at device registration.
            copyOptional(request, EulerOAuth2ParameterNames.ATTESTATION, additionalParams);

            // Unlike standard JWT headers, Apple App Attest attestation and assertion data
            // cannot carry a kid, so it must be sent as a separate parameter.
            copyRequired(request, EulerOAuth2ParameterNames.KEY_ID, additionalParams);

            copyRequired(request, EulerOAuth2ParameterNames.CHALLENGE, additionalParams);
            copyRequired(request, EulerOAuth2ParameterNames.ASSERTION, additionalParams);
        }

        // Optional request client_id (for RFC6749 consistency check in Provider)
        copyOptional(request, OAuth2ParameterNames.CLIENT_ID, additionalParams);

        return new OAuth2ClientAuthenticationToken(
                ATTESTATION_PRINCIPAL_PLACEHOLDER,
                EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH,
                null, additionalParams);
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

    private static void copyRequired(Object value, String paramName,
                                     Map<String, Object> target) {
        if (value == null) {
            throw newError(OAuth2ErrorCodes.INVALID_REQUEST, paramName, null);
        }
        target.put(paramName, value);
    }

    private static void copyRequired(HttpServletRequest request, String paramName,
                                     Map<String, Object> target) {
        String value = request.getParameter(paramName);
        if (!StringUtils.hasText(value)) {
            throw newError(OAuth2ErrorCodes.INVALID_REQUEST, paramName, null);
        }
        target.put(paramName, value);
    }

    private static OAuth2AuthenticationException newError(String errorCode, String parameterName, String errorUri) {
        OAuth2Error error = new OAuth2Error(errorCode, "OAuth 2.0 Parameter: " + parameterName, errorUri);
        return new OAuth2AuthenticationException(error);
    }
}
