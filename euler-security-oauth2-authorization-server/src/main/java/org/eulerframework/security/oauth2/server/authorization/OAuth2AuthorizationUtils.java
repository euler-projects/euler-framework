package org.eulerframework.security.oauth2.server.authorization;

import org.eulerframework.common.util.StringUtils;
import org.eulerframework.security.oauth2.core.oidc.EulerOidcScopes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.util.CollectionUtils;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class OAuth2AuthorizationUtils {
    public static void putExtendClaims(OAuth2Authorization authorization, Set<String> scopes, Map<String, Object> claims) {
        if (scopes == null || scopes.isEmpty()) {
            return;
        }

        if(!scopes.contains(EulerOidcScopes.AUTHORITIES)
                && !scopes.contains(OidcScopes.PROFILE)) {
            return;
        }

        // The attribute holds whatever the grant provider authenticated the user with: a bare
        // UserDetails, or any Authentication carrying one as its principal. Matching on that shape
        // rather than on concrete token types keeps grants whose tokens are Euler-specific covered.
        Object principalAttribute = authorization.getAttribute(Principal.class.getName());

        UserDetails userDetails = null;
        Collection<? extends GrantedAuthority> tokenAuthorities = null;
        if (principalAttribute instanceof UserDetails principal) {
            userDetails = principal;
        } else if (principalAttribute instanceof Authentication authentication) {
            tokenAuthorities = authentication.getAuthorities();
            if (authentication.getPrincipal() instanceof UserDetails principal) {
                userDetails = principal;
            }
        }

        String username = null;
        Collection<? extends GrantedAuthority> authorities = null;
        if (userDetails != null) {
            if (scopes.contains(OidcScopes.PROFILE)) {
                username = userDetails.getUsername();
            }
            if (scopes.contains(EulerOidcScopes.AUTHORITIES)) {
                authorities = userDetails.getAuthorities();
            }
        } else if (tokenAuthorities != null && scopes.contains(EulerOidcScopes.AUTHORITIES)) {
            authorities = tokenAuthorities;
        }

        if (StringUtils.hasText(username)) {
            claims.put(StandardClaimNames.PREFERRED_USERNAME, username);
        }

        if (!CollectionUtils.isEmpty(authorities)) {
            claims.put("authorities", authorities);
        }
    }
}
