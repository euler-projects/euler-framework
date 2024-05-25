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
package org.eulerframework.security.oauth2.resource;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.util.Assert;

import java.util.*;

public class OAuth2NativeTokenAuthenticationManager implements AuthenticationManager {
    private static final String AUTHORITY_PREFIX = "SCOPE_";

    private final OAuth2AuthorizationService authorizationService;

    public OAuth2NativeTokenAuthenticationManager(OAuth2AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(BearerTokenAuthenticationToken.class, authentication,
                () -> "Unsupported authentication type: " + authentication.getClass());
        BearerTokenAuthenticationToken bearerTokenAuthenticationToken = (BearerTokenAuthenticationToken) authentication;

        OAuth2Authorization authorization = authorizationService.findByToken(bearerTokenAuthenticationToken.getToken(), OAuth2TokenType.ACCESS_TOKEN);

        OAuth2Authorization.Token<OAuth2AccessToken> authorizedToken = Optional.ofNullable(authorization)
                .map(az -> az.getToken(OAuth2AccessToken.class))
                .filter(OAuth2Authorization.Token::isActive)
                .orElse(null);
        if (authorizedToken == null) {
            throw new BadCredentialsException("Invalid bearer token");
        }

        OAuth2AccessToken accessToken = authorizedToken.getToken();
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Optional.ofNullable(accessToken.getScopes()).orElse(Collections.emptySet())
                .stream()
                .map(scope -> AUTHORITY_PREFIX + scope)
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        Collection<GrantedAuthority> resourceOwnerAuthorities = null;
        Object resourceOwnerPrincipal = authorization.getAttribute("java.security.Principal");
        if(resourceOwnerPrincipal instanceof UsernamePasswordAuthenticationToken) {
            resourceOwnerAuthorities = ((UsernamePasswordAuthenticationToken) resourceOwnerPrincipal).getAuthorities();
        }

        Map<String, Object> claims = authorizedToken.getClaims();
        Map<String, Object> attributes = new HashMap<>();
        if (claims != null) {
            attributes.putAll(claims);
        }
        attributes.putAll(authorization.getAttributes());
        attributes.put(OAuth2TokenIntrospectionClaimNames.CLIENT_ID, authorization.getRegisteredClientId());

        OAuth2AuthenticatedPrincipal principal = new NativeOAuth2AuthenticatedPrincipal(attributes, authorities, resourceOwnerAuthorities);
        return new BearerTokenAuthentication(principal, accessToken, authorities);
    }
}
