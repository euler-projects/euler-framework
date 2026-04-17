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

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;

import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;

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
 * {@link org.eulerframework.security.oauth2.server.authorization.authentication.ClientAttestationAuthenticationProvider
 * ClientAttestationAuthenticationProvider}.
 *
 * @see EulerOAuth2ParameterNames
 * @see EulerClientAuthenticationMethod#ATTEST_JWT_CLIENT_AUTH
 */
public final class ClientAttestationAuthenticationConverter implements AuthenticationConverter {

    /**
     * Placeholder principal used for the unauthenticated token. The real {@code client_id}
     * is resolved by the provider after attestation verification.
     */
    static final String ATTESTATION_PRINCIPAL_PLACEHOLDER = "__attestation__";

    @Override
    public Authentication convert(HttpServletRequest request) {
        // 1. Check for attestation signal
        String attestationJwt = request.getHeader(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION);
        String popData = request.getHeader(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP);
        String popTypeHeader = request.getHeader(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP_TYPE);

        boolean hasAttestation = attestationJwt != null || popData != null || popTypeHeader != null;
        if (!hasAttestation) {
            return null;
        }

        String popType = popTypeHeader != null ? popTypeHeader : EulerOAuth2ParameterNames.POP_TYPE_JWT;

        // 2. Collect all raw attestation data — no parsing, no DB lookup
        Map<String, Object> additionalParams = new LinkedHashMap<>();
        if (attestationJwt != null) {
            additionalParams.put(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION, attestationJwt);
        }
        if (popData != null) {
            additionalParams.put(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP, popData);
        }
        additionalParams.put(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP_TYPE, popType);

        // App-Attest parameters (may be null; Provider decides based on popType)
        copyIfPresent(request, EulerOAuth2ParameterNames.KEY_ID, additionalParams);
        copyIfPresent(request, EulerOAuth2ParameterNames.ASSERTION, additionalParams);
        copyIfPresent(request, EulerOAuth2ParameterNames.CHALLENGE, additionalParams);

        // Optional request client_id (for RFC6749 consistency check in Provider)
        copyIfPresent(request, OAuth2ParameterNames.CLIENT_ID, additionalParams);

        return new OAuth2ClientAuthenticationToken(
                ATTESTATION_PRINCIPAL_PLACEHOLDER,
                EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH,
                null, additionalParams);
    }

    private static void copyIfPresent(HttpServletRequest request, String paramName,
                                       Map<String, Object> target) {
        String value = request.getParameter(paramName);
        if (value != null) {
            target.put(paramName, value);
        }
    }
}
