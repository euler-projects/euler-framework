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
package org.eulerframework.security.oauth2.resource.context;

import org.eulerframework.common.util.StringUtils;
import org.eulerframework.security.core.context.UserContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

public class BearerTokenAuthenticationUserContext implements UserContext {
    @Override
    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !BearerTokenAuthentication.class.isAssignableFrom(authentication.getClass())) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        OAuth2AuthenticatedPrincipal authenticatedPrincipal = (OAuth2AuthenticatedPrincipal) principal;
        String sub = authenticatedPrincipal.getAttribute(OAuth2TokenIntrospectionClaimNames.SUB);
        return StringUtils.hasText(sub) ? sub : authenticatedPrincipal.getName();
    }

    @Override
    public String getTenantId() {
        return "1";
    }
}
