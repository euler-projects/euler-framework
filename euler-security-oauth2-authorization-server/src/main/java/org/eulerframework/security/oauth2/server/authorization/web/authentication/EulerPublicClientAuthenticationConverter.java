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
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Attempts to extract a {@code client_id} from {@link HttpServletRequest} parameters
 * for authenticating public OAuth 2.0 clients (those using {@link ClientAuthenticationMethod#NONE}).
 * <p>
 * Unlike Spring's {@link org.springframework.security.oauth2.server.authorization.web.authentication.PublicClientAuthenticationConverter
 * PublicClientAuthenticationConverter}, which only matches PKCE token requests
 * ({@code grant_type=authorization_code} + {@code code_verifier}), this converter
 * matches any request that carries a {@code client_id} parameter without other
 * client authentication credentials.
 * <p>
 * This converter is intended to be placed <b>last</b> in a
 * {@link org.springframework.security.web.authentication.DelegatingAuthenticationConverter
 * DelegatingAuthenticationConverter} chain so that it acts as a fallback when no
 * other converter matches.
 * <p>
 * Requests that carry any of the following are skipped (returning {@code null}) so
 * that the appropriate converter handles them instead:
 * <ul>
 *     <li>{@code Authorization} header (Basic or Bearer authentication)</li>
 *     <li>{@code client_secret} parameter (confidential client via POST)</li>
 *     <li>{@code client_assertion} parameter (JWT client assertion)</li>
 * </ul>
 *
 * @see org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken
 * @see org.eulerframework.security.oauth2.server.authorization.authentication.EulerPublicClientAuthenticationProvider
 */
public final class EulerPublicClientAuthenticationConverter implements AuthenticationConverter {

    @Nullable
    @Override
    public Authentication convert(@Nonnull HttpServletRequest request) {
        MultiValueMap<String, String> parameters = getFormParameters(request);

        // client_id (REQUIRED for public clients)
        String clientId = parameters.getFirst(OAuth2ParameterNames.CLIENT_ID);
        if (!StringUtils.hasText(clientId)) {
            return null;
        }

        if (parameters.get(OAuth2ParameterNames.CLIENT_ID).size() != 1) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
        }

        // Skip if other client authentication mechanisms are present
        if (request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
            return null;
        }
        if (parameters.containsKey(OAuth2ParameterNames.CLIENT_SECRET)) {
            return null;
        }
        if (parameters.containsKey(OAuth2ParameterNames.CLIENT_ASSERTION)) {
            return null;
        }

        parameters.remove(OAuth2ParameterNames.CLIENT_ID);

        Map<String, Object> additionalParameters = new HashMap<>();
        parameters.forEach((key, value) -> additionalParameters.put(key,
                (value.size() == 1) ? value.get(0) : value.toArray(new String[0])));

        return new OAuth2ClientAuthenticationToken(clientId, ClientAuthenticationMethod.NONE, null,
                additionalParameters);
    }

    private static MultiValueMap<String, String> getFormParameters(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> {
            for (String value : values) {
                parameters.add(key, value);
            }
        });
        return parameters;
    }
}
