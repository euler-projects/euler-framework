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
package org.springframework.security.oauth2.server.authorization.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.converter.ClaimConversionService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenIntrospection;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.net.URL;
import java.security.Principal;
import java.util.*;

import static org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient;

public final class OAuth2PrincipalSupportTokenIntrospectionAuthenticationProvider implements AuthenticationProvider {
    private static final String INTROSPECT_ORIGINAL_PRINCIPAL_SCOPE = "introspect-original-principal";

    private static final TypeDescriptor OBJECT_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(Object.class);
    private static final TypeDescriptor LIST_STRING_TYPE_DESCRIPTOR =
            TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(String.class));
    private final Log logger = LogFactory.getLog(getClass());
    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2AuthorizationService authorizationService;

    /**
     * Constructs an {@code OAuth2TokenIntrospectionAuthenticationProvider} using the provided parameters.
     *
     * @param registeredClientRepository the repository of registered clients
     * @param authorizationService       the authorization service
     */
    public OAuth2PrincipalSupportTokenIntrospectionAuthenticationProvider(RegisteredClientRepository registeredClientRepository,
                                                                          OAuth2AuthorizationService authorizationService) {
        Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        this.registeredClientRepository = registeredClientRepository;
        this.authorizationService = authorizationService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2TokenIntrospectionAuthenticationToken tokenIntrospectionAuthentication =
                (OAuth2TokenIntrospectionAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal =
                getAuthenticatedClientElseThrowInvalidClient(tokenIntrospectionAuthentication);

        OAuth2Authorization authorization = this.authorizationService.findByToken(
                tokenIntrospectionAuthentication.getToken(), null);
        if (authorization == null) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Did not authenticate token introspection request since token was not found");
            }
            // Return the authentication request when token not found
            return tokenIntrospectionAuthentication;
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Retrieved authorization with token");
        }

        OAuth2Authorization.Token<OAuth2Token> authorizedToken =
                authorization.getToken(tokenIntrospectionAuthentication.getToken());
        if (!authorizedToken.isActive()) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Did not introspect token since not active");
            }
            return new OAuth2TokenIntrospectionAuthenticationToken(tokenIntrospectionAuthentication.getToken(),
                    clientPrincipal, OAuth2TokenIntrospection.builder().build());
        }

        RegisteredClient authorizedClient = this.registeredClientRepository.findById(authorization.getRegisteredClientId());

        UserDetails grantUserDetails = null;
        if (AuthorizationGrantType.PASSWORD.equals(authorization.getAuthorizationGrantType())
                && !CollectionUtils.isEmpty(authorization.getAuthorizedScopes())
                && authorization.getAuthorizedScopes().contains(INTROSPECT_ORIGINAL_PRINCIPAL_SCOPE)) {
            Principal principal = authorization.getAttribute(Principal.class.getName());
            if (principal instanceof UsernamePasswordAuthenticationToken) {
                Object userPrincipal = ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
                if (userPrincipal instanceof UserDetails) {
                    grantUserDetails = (UserDetails) userPrincipal;
                }
            }
        }

        OAuth2TokenIntrospection tokenClaims = withActiveTokenClaims(authorizedToken, authorizedClient, grantUserDetails);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Authenticated token introspection request");
        }

        return new

                OAuth2TokenIntrospectionAuthenticationToken(authorizedToken.getToken().

                getTokenValue(),

                clientPrincipal, tokenClaims);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2TokenIntrospectionAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private static OAuth2TokenIntrospection withActiveTokenClaims(
            OAuth2Authorization.Token<OAuth2Token> authorizedToken, RegisteredClient authorizedClient, UserDetails grantUserDetails) {

        OAuth2TokenIntrospection.Builder tokenClaims;
        if (!CollectionUtils.isEmpty(authorizedToken.getClaims())) {
            Map<String, Object> claims = convertClaimsIfNecessary(authorizedToken.getClaims());

            if (grantUserDetails != null) {
                claims.put(EulerOAuth2TokenIntrospectionClaimNames.SUB_DETAILS, grantUserDetails);
            }

            tokenClaims = OAuth2TokenIntrospection.withClaims(claims).active(true);
        } else {
            if (grantUserDetails != null) {
                Map<String, Object> claims = Collections.singletonMap(EulerOAuth2TokenIntrospectionClaimNames.SUB_DETAILS, grantUserDetails);
                tokenClaims = OAuth2TokenIntrospection.withClaims(claims).active(true);
            } else {
                tokenClaims = OAuth2TokenIntrospection.builder(true);
            }
        }

        tokenClaims.clientId(authorizedClient.getClientId());

        // TODO Set "username"

        OAuth2Token token = authorizedToken.getToken();
        if (token.getIssuedAt() != null) {
            tokenClaims.issuedAt(token.getIssuedAt());
        }
        if (token.getExpiresAt() != null) {
            tokenClaims.expiresAt(token.getExpiresAt());
        }

        if (OAuth2AccessToken.class.isAssignableFrom(token.getClass())) {
            OAuth2AccessToken accessToken = (OAuth2AccessToken) token;
            tokenClaims.tokenType(accessToken.getTokenType().getValue());
        }

        return tokenClaims.build();
    }

    private static Map<String, Object> convertClaimsIfNecessary(Map<String, Object> claims) {
        Map<String, Object> convertedClaims = new HashMap<>(claims);

        Object value = claims.get(OAuth2TokenIntrospectionClaimNames.ISS);
        if (value != null && !(value instanceof URL)) {
            URL convertedValue = ClaimConversionService.getSharedInstance()
                    .convert(value, URL.class);
            if (convertedValue != null) {
                convertedClaims.put(OAuth2TokenIntrospectionClaimNames.ISS, convertedValue);
            }
        }

        value = claims.get(OAuth2TokenIntrospectionClaimNames.SCOPE);
        if (value != null && !(value instanceof List)) {
            Object convertedValue = ClaimConversionService.getSharedInstance()
                    .convert(value, OBJECT_TYPE_DESCRIPTOR, LIST_STRING_TYPE_DESCRIPTOR);
            if (convertedValue != null) {
                convertedClaims.put(OAuth2TokenIntrospectionClaimNames.SCOPE, convertedValue);
            }
        }

        value = claims.get(OAuth2TokenIntrospectionClaimNames.AUD);
        if (value != null && !(value instanceof List)) {
            Object convertedValue = ClaimConversionService.getSharedInstance()
                    .convert(value, OBJECT_TYPE_DESCRIPTOR, LIST_STRING_TYPE_DESCRIPTOR);
            if (convertedValue != null) {
                convertedClaims.put(OAuth2TokenIntrospectionClaimNames.AUD, convertedValue);
            }
        }

        return convertedClaims;
    }

}
