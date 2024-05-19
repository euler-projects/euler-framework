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
