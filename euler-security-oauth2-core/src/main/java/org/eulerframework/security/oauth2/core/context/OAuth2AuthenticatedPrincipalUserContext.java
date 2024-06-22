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
package org.eulerframework.security.oauth2.core.context;

import org.eulerframework.security.core.context.UserContext;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.oauth2.core.userdetails.provider.OAuth2TokenUserDetailsProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.eulerframework.security.oauth2.core.EulerOAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OAuth2AuthenticatedPrincipalUserContext implements UserContext {
    private final List<OAuth2TokenUserDetailsProvider> tokenUserDetailsProviders = new ArrayList<>();

    public OAuth2AuthenticatedPrincipalUserContext(List<? extends OAuth2TokenUserDetailsProvider> tokenUserDetailsProviders) {
        Assert.notEmpty(tokenUserDetailsProviders, "at least one OAuth2TokenUserDetailsProvider is required");
        this.tokenUserDetailsProviders.addAll(tokenUserDetailsProviders);
    }

    @Override
    public EulerUserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
            /*
             * Euler OAuth2.0 resource server security chain has attempted to analyse the user's information
             * who authorized the token and store it use the extended claim 'sub_details'. Try to get it.
             */
            EulerUserDetails eulerUserDetails = principal.getAttribute(EulerOAuth2TokenIntrospectionClaimNames.SUB_DETAILS);
            if (eulerUserDetails != null) {
                return eulerUserDetails;
            }

            /*
             * If application defined OAuth2TokenUserDetailsProvider,
             * try to analyse the Subject of the OAuth 2.0 token,
             * usually a machine-readable identifier of the resource owner who authorized the token.
             * See reference: https://datatracker.ietf.org/doc/html/rfc7662#section-2.2
             */
            if (CollectionUtils.isEmpty(this.tokenUserDetailsProviders)) {
                return null;
            }
            String tokenSubject = principal.getAttribute(OAuth2TokenIntrospectionClaimNames.SUB);
            if (!StringUtils.hasText(tokenSubject)) {
                return null;
            }
            EulerUserDetails userDetails;
            for (OAuth2TokenUserDetailsProvider tokenUserDetailsProvider : tokenUserDetailsProviders) {
                if ((userDetails = tokenUserDetailsProvider.provide(tokenSubject)) != null) {
                    return userDetails;
                }
            }
        }

        return null;
    }

    @Override
    public String getUserId() {
        EulerUserDetails userDetails = getUserDetails();
        return userDetails == null ? null : userDetails.getUserId();
    }

    /**
     * Return the Subject of the OAuth 2.0 token,
     * usually a machine-readable identifier of the resource owner who authorized the token.
     * See reference
     * <a target="_blank" href=
     * "https://datatracker.ietf.org/doc/html/rfc7662#section-2.2">OAuth 2.0 Token Introspection Section 2.2</a>
     *
     * @return the Subject of the token
     */
    private String getTokenSubject() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
            return principal.getAttribute(OAuth2TokenIntrospectionClaimNames.SUB);
        }

        return null;
    }
}
