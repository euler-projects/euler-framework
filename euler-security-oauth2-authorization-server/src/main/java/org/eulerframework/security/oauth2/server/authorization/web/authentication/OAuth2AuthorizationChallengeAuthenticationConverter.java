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
import org.eulerframework.security.authentication.otp.OneTimePasswordAuthenticationToken;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;
import org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationChallengeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2EndpointUtilsAccessor;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Parses a {@code POST /authorize-challenge} request (Section 5.1 of
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-first-party-apps-04.txt">
 * draft-ietf-oauth-first-party-apps-04</a>) into an
 * {@link OAuth2AuthorizationChallengeAuthenticationToken}.
 * <p>
 * The client is authenticated by a preceding client authentication filter, so the authenticated client
 * principal is read from the {@link SecurityContextHolder} (exactly as this framework's grant converters
 * do) and carried as the token's principal; a {@code client_id} parameter, when present, is only checked
 * for consistency with it (Section 5.1). Recognized parameters: {@code response_type} (REQUIRED, must be
 * {@code code}), {@code scope} (OPTIONAL), {@code code_challenge} / {@code code_challenge_method}
 * (OPTIONAL, PKCE), and this framework's {@code otp_ticket} + {@code otp} user-authentication extension.
 * The endpoint is redirect-less, so {@code redirect_uri} is not read.
 * <p>
 * The sensitive {@code otp} never lands in a parameter map that could reach audit or logging: it travels
 * only inside the {@link OneTimePasswordAuthenticationToken} credentials, mirroring the {@code otp} grant.
 */
public final class OAuth2AuthorizationChallengeAuthenticationConverter implements AuthenticationConverter {

    private static final String DEFAULT_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1";

    @Override
    public Authentication convert(HttpServletRequest request) {
        // The client authentication filter runs first and leaves the authenticated client on the context.
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        RegisteredClient registeredClient = resolveAuthenticatedClient(clientPrincipal);

        MultiValueMap<String, String> parameters = OAuth2EndpointUtilsAccessor.getFormParameters(request);

        // response_type (REQUIRED) - must be "code" (Section 5.1)
        String responseType = parameters.getFirst(OAuth2ParameterNames.RESPONSE_TYPE);
        if (!OAuth2AuthorizationResponseType.CODE.getValue().equals(responseType)) {
            throw invalidRequest(OAuth2ParameterNames.RESPONSE_TYPE);
        }

        // client_id (OPTIONAL) - when present it MUST identify the same client (Section 5.1)
        String clientId = parameters.getFirst(OAuth2ParameterNames.CLIENT_ID);
        if (StringUtils.hasText(clientId) && !registeredClient.getClientId().equals(clientId)) {
            throw invalidRequest(OAuth2ParameterNames.CLIENT_ID);
        }

        // otp_ticket (REQUIRED)
        String otpTicket = parameters.getFirst(EulerOAuth2ParameterNames.OTP_TICKET);
        if (!StringUtils.hasText(otpTicket)) {
            throw invalidRequest(EulerOAuth2ParameterNames.OTP_TICKET);
        }

        // otp (REQUIRED)
        String otp = parameters.getFirst(EulerOAuth2ParameterNames.OTP);
        if (!StringUtils.hasText(otp)) {
            throw invalidRequest(EulerOAuth2ParameterNames.OTP);
        }

        // scope (OPTIONAL, single space-delimited value)
        Set<String> scopes = null;
        String scope = parameters.getFirst(OAuth2ParameterNames.SCOPE);
        if (StringUtils.hasText(scope)) {
            if (parameters.get(OAuth2ParameterNames.SCOPE).size() != 1) {
                throw invalidRequest(OAuth2ParameterNames.SCOPE);
            }
            scopes = new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
        }

        // code_challenge / code_challenge_method (OPTIONAL, PKCE - RFC 7636)
        String codeChallenge = parameters.getFirst(PkceParameterNames.CODE_CHALLENGE);
        String codeChallengeMethod = parameters.getFirst(PkceParameterNames.CODE_CHALLENGE_METHOD);

        String authorizationUri = request.getRequestURL().toString();

        return new OAuth2AuthorizationChallengeAuthenticationToken(
                clientPrincipal,
                new OneTimePasswordAuthenticationToken(otpTicket, otp),
                authorizationUri,
                scopes,
                codeChallenge,
                codeChallengeMethod);
    }

    private static RegisteredClient resolveAuthenticatedClient(Authentication clientPrincipal) {
        if (clientPrincipal instanceof OAuth2ClientAuthenticationToken clientAuthentication
                && clientAuthentication.isAuthenticated()
                && clientAuthentication.getRegisteredClient() != null) {
            return clientAuthentication.getRegisteredClient();
        }
        throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT,
                "Client authentication is required at the authorization challenge endpoint.", DEFAULT_ERROR_URI));
    }

    private static OAuth2AuthenticationException invalidRequest(String parameterName) {
        return new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST,
                "OAuth 2.0 Parameter: " + parameterName, DEFAULT_ERROR_URI));
    }
}
