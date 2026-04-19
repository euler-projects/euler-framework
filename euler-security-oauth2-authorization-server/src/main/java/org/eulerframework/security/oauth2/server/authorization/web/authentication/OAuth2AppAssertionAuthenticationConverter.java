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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2EndpointUtilsAccessor;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import org.eulerframework.security.oauth2.core.EulerAuthorizationGrantType;
import org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2AppAssertionAuthenticationToken;
import org.eulerframework.security.oauth2.server.authorization.web.EulerOAuth2AttestationBasedClientAuthenticationFilter;

/**
 * Converts HTTP requests for the {@code urn:ietf:params:oauth:grant-type:app_assertion} grant type into
 * {@link OAuth2AppAssertionAuthenticationToken} instances.
 * <p>
 * This converter reads the verified {@code keyId} from a request attribute
 * ({@link EulerOAuth2AttestationBasedClientAuthenticationFilter#ATTESTATION_VERIFIED_KEY_ID_ATTRIBUTE}) set by
 * {@link EulerOAuth2AttestationBasedClientAuthenticationFilter}. If the attribute is absent, the converter returns
 * {@code null} (indicating the request did not pass attestation verification).
 * <p>
 * Assertion and challenge parameters ({@code kid}, {@code assertion},
 * {@code challenge}) are no longer extracted here — they are consumed by
 * {@link EulerOAuth2AttestationBasedClientAuthenticationFilter} during PoP verification.
 */
public class OAuth2AppAssertionAuthenticationConverter implements AuthenticationConverter {
    private static final String DEFAULT_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

    @Override
    public Authentication convert(HttpServletRequest request) {
        MultiValueMap<String, String> parameters = OAuth2EndpointUtilsAccessor.getFormParameters(request);

        // grant_type (REQUIRED)
        String grantType = parameters.getFirst(OAuth2ParameterNames.GRANT_TYPE);
        if (!EulerAuthorizationGrantType.APP_ASSERTION.getValue().equals(grantType)) {
            return null;
        }

        // keyId from request attribute (set by EulerOAuth2AttestationBasedClientAuthenticationFilter after successful verification)
        String keyId = (String) request.getAttribute(
                EulerOAuth2AttestationBasedClientAuthenticationFilter.ATTESTATION_VERIFIED_KEY_ID_ATTRIBUTE);
        if (keyId == null) {
            // Filter did not verify attestation → this request is not attestation-backed
            return null;
        }

        // scope (OPTIONAL)
        Set<String> scopes = null;
        String scope = parameters.getFirst(OAuth2ParameterNames.SCOPE);
        if (StringUtils.hasText(scope) &&
                parameters.get(OAuth2ParameterNames.SCOPE).size() != 1) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST,
                    "OAuth 2.0 Parameter format error: " + OAuth2ParameterNames.SCOPE, DEFAULT_ERROR_URI));
        }
        if (StringUtils.hasText(scope)) {
            scopes = new HashSet<>(
                    Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
        }

        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> additionalParameters = new HashMap<>();
        parameters.forEach((key, value) -> {
            if (!key.equals(OAuth2ParameterNames.GRANT_TYPE) &&
                    !key.equals(OAuth2ParameterNames.SCOPE)) {
                additionalParameters.put(key, (value.size() == 1) ? value.get(0) : value.toArray(new String[0]));
            }
        });

        return new OAuth2AppAssertionAuthenticationToken(
                keyId,
                clientPrincipal,
                scopes,
                additionalParameters
        );
    }
}
