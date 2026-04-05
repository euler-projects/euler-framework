/*
 * Copyright 2013-2026 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * An {@link AuthenticationProvider} for OAuth 2.0 public client authentication
 * that supports <b>any</b> grant type — not just {@code authorization_code} with PKCE.
 * <p>
 * Spring's built-in
 * {@link org.springframework.security.oauth2.server.authorization.authentication.PublicClientAuthenticationProvider
 * PublicClientAuthenticationProvider} is tightly coupled to the PKCE flow: its
 * {@code CodeVerifierAuthenticator} always requires a valid {@code code_verifier}
 * and throws {@code invalid_grant} for non-{@code authorization_code} requests.
 * This makes it unusable for custom endpoints (e.g., a challenge endpoint) or
 * custom grant types (e.g., Apple App Attest) where a public client needs to
 * authenticate without PKCE.
 * <p>
 * This provider fills that gap by performing only the essential public-client
 * checks:
 * <ol>
 *     <li>The {@link ClientAuthenticationMethod} must be {@link ClientAuthenticationMethod#NONE NONE}.</li>
 *     <li>The {@code authorization_code} grant type is <b>explicitly skipped</b>
 *         (returns {@code null}) so that Spring's own provider handles PKCE.</li>
 *     <li>The client must exist in the {@link RegisteredClientRepository}.</li>
 *     <li>The client's registered authentication methods must include
 *         {@link ClientAuthenticationMethod#NONE NONE}.</li>
 * </ol>
 *
 * @see org.eulerframework.security.oauth2.server.authorization.web.authentication.EulerPublicClientAuthenticationConverter
 * @see org.springframework.security.oauth2.server.authorization.authentication.PublicClientAuthenticationProvider
 */
public final class EulerPublicClientAuthenticationProvider implements AuthenticationProvider {

    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-3.2.1";

    private final Log logger = LogFactory.getLog(getClass());

    private final RegisteredClientRepository registeredClientRepository;

    public EulerPublicClientAuthenticationProvider(RegisteredClientRepository registeredClientRepository) {
        Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2ClientAuthenticationToken clientAuthentication = (OAuth2ClientAuthenticationToken) authentication;

        if (!ClientAuthenticationMethod.NONE.equals(clientAuthentication.getClientAuthenticationMethod())) {
            return null;
        }

        // Skip authorization_code grants — let Spring's PublicClientAuthenticationProvider
        // handle them with proper PKCE validation.
        Map<String, Object> parameters = clientAuthentication.getAdditionalParameters();
        if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue()
                .equals(parameters.get(OAuth2ParameterNames.GRANT_TYPE))) {
            return null;
        }

        String clientId = clientAuthentication.getPrincipal().toString();
        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            throwInvalidClient(OAuth2ParameterNames.CLIENT_ID);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Retrieved registered client");
        }

        if (!registeredClient.getClientAuthenticationMethods()
                .contains(clientAuthentication.getClientAuthenticationMethod())) {
            throwInvalidClient("authentication_method");
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Authenticated public client");
        }

        return new OAuth2ClientAuthenticationToken(registeredClient,
                clientAuthentication.getClientAuthenticationMethod(), null);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private static void throwInvalidClient(String parameterName) {
        OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT,
                "Client authentication failed: " + parameterName, ERROR_URI);
        throw new OAuth2AuthenticationException(error);
    }
}
