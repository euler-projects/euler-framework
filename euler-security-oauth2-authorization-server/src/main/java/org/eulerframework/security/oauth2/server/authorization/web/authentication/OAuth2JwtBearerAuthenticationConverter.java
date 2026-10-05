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

import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2JwtBearerAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2EndpointUtilsAccessor;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parses {@code POST /oauth2/token} form parameters when
 * {@code grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer} and builds an
 * {@link OAuth2JwtBearerAuthenticationToken}.
 * <p>
 * Required parameter: {@code assertion} (RFC 7523 Section 2.1). Optional: {@code scope}
 * (single, space-delimited).
 * <p>
 * The {@code assertion} is stripped from {@code additionalParameters} so a signed
 * credential cannot leak through audit or logging, mirroring how the {@code otp} grant
 * treats its one-time password.
 */
public class OAuth2JwtBearerAuthenticationConverter implements AuthenticationConverter {

    private static final String DEFAULT_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

    @Override
    public Authentication convert(HttpServletRequest request) {
        MultiValueMap<String, String> parameters = OAuth2EndpointUtilsAccessor.getFormParameters(request);

        // grant_type (REQUIRED)
        String grantType = parameters.getFirst(OAuth2ParameterNames.GRANT_TYPE);
        if (!AuthorizationGrantType.JWT_BEARER.getValue().equals(grantType)) {
            return null;
        }

        // assertion (REQUIRED) - strip from parameters to avoid leaking via additionalParameters
        String assertion = parameters.getFirst(OAuth2ParameterNames.ASSERTION);
        if (!StringUtils.hasText(assertion)) {
            throw invalidRequest(OAuth2ParameterNames.ASSERTION);
        }
        parameters.remove(OAuth2ParameterNames.ASSERTION);

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
                    !key.equals(OAuth2ParameterNames.ASSERTION) &&
                    !key.equals(OAuth2ParameterNames.SCOPE)) {
                additionalParameters.put(key, (value.size() == 1) ? value.get(0) : value.toArray(new String[0]));
            }
        });

        return new OAuth2JwtBearerAuthenticationToken(
                assertion, clientPrincipal, scopes, additionalParameters);
    }

    private static OAuth2AuthenticationException invalidRequest(String parameterName) {
        return new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST,
                "OAuth 2.0 Parameter: " + parameterName, DEFAULT_ERROR_URI));
    }
}
