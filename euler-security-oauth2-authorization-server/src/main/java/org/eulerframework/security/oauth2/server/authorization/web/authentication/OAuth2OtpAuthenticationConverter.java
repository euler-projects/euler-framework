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
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.oauth2.core.EulerAuthorizationGrantType;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;
import org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2OtpAuthenticationToken;
import org.eulerframework.security.oauth2.server.authorization.web.EulerOAuth2AttestationBasedClientAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parses {@code POST /oauth2/token} form parameters when {@code grant_type=otp}
 * and builds an {@link OAuth2OtpAuthenticationToken}.
 * <p>
 * Required parameters: {@code otp_ticket}, {@code otp}; {@code code_verifier}
 * is required only when PKCE is enabled
 * ({@code euler.security.otp.pkce.enabled=true}). Optional: {@code scope}
 * (single, space-delimited).
 * <p>
 * Sensitive parameters ({@code otp}, {@code code_verifier}) are stripped from
 * {@code additionalParameters} so they cannot leak through audit / logging.
 */
public class OAuth2OtpAuthenticationConverter implements AuthenticationConverter {

    private static final String DEFAULT_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

    private final boolean pkceRequired;

    /**
     * Backwards-compatible constructor: PKCE required.
     */
    public OAuth2OtpAuthenticationConverter() {
        this(true);
    }

    public OAuth2OtpAuthenticationConverter(boolean pkceRequired) {
        this.pkceRequired = pkceRequired;
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        MultiValueMap<String, String> parameters = getFormParameters(request);

        // grant_type (REQUIRED)
        String grantType = parameters.getFirst(OAuth2ParameterNames.GRANT_TYPE);
        if (!EulerAuthorizationGrantType.OTP.getValue().equals(grantType)) {
            return null;
        }

        // otp_ticket (REQUIRED)
        String otpTicket = parameters.getFirst(EulerOAuth2ParameterNames.OTP_TICKET);
        if (!StringUtils.hasText(otpTicket)) {
            throw invalidRequest(EulerOAuth2ParameterNames.OTP_TICKET);
        }

        // otp (REQUIRED) - strip from parameters to avoid leaking via additionalParameters
        String otp = parameters.getFirst(EulerOAuth2ParameterNames.OTP);
        if (!StringUtils.hasText(otp)) {
            throw invalidRequest(EulerOAuth2ParameterNames.OTP);
        }
        parameters.remove(EulerOAuth2ParameterNames.OTP);

        // code_verifier (REQUIRED when PKCE is enabled, PKCE - RFC 7636).
        // Strip from parameters either way so it never leaks through
        // additionalParameters into audit / logging.
        String codeVerifier = parameters.getFirst(PkceParameterNames.CODE_VERIFIER);
        if (this.pkceRequired && !StringUtils.hasText(codeVerifier)) {
            throw invalidRequest(PkceParameterNames.CODE_VERIFIER);
        }
        if (!this.pkceRequired) {
            // Don't carry a value the provider would forward to consume();
            // null forces OtpPkceVerifier to skip the check.
            codeVerifier = null;
        }
        parameters.remove(PkceParameterNames.CODE_VERIFIER);

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

        // Optional verified App Attest registration carried by
        // EulerOAuth2AttestationBasedClientAuthenticationFilter. When present,
        // the provider enforces device-to-user consistency and auto-binds the
        // device to the OTP-resolved user on first use.
        AppAttestAttestationRegistration verifiedAppRegistration = (AppAttestAttestationRegistration) request.getAttribute(
                EulerOAuth2AttestationBasedClientAuthenticationFilter.VERIFIED_CLIENT_ATTESTATION_ATTRIBUTE);

        Map<String, Object> additionalParameters = new HashMap<>();
        parameters.forEach((key, value) -> {
            if (!key.equals(OAuth2ParameterNames.GRANT_TYPE) &&
                    !key.equals(EulerOAuth2ParameterNames.OTP_TICKET) &&
                    !key.equals(EulerOAuth2ParameterNames.OTP) &&
                    !key.equals(PkceParameterNames.CODE_VERIFIER) &&
                    !key.equals(OAuth2ParameterNames.SCOPE)) {
                additionalParameters.put(key, (value.size() == 1) ? value.get(0) : value.toArray(new String[0]));
            }
        });

        return new OAuth2OtpAuthenticationToken(
                otpTicket, otp, codeVerifier,
                clientPrincipal, scopes, additionalParameters, verifiedAppRegistration);
    }

    private static OAuth2AuthenticationException invalidRequest(String parameterName) {
        return new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST,
                "OAuth 2.0 Parameter: " + parameterName, DEFAULT_ERROR_URI));
    }

    private MultiValueMap<String, String> getFormParameters(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> {
            String queryString = StringUtils.hasText(request.getQueryString()) ? request.getQueryString() : "";
            // If not query parameter then it's a form parameter
            if (!queryString.contains(key)) {
                for (String value : values) {
                    parameters.add(key, value);
                }
            }
        });
        return parameters;
    }
}
