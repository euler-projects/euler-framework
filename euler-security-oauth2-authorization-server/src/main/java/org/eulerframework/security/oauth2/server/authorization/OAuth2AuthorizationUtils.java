package org.eulerframework.security.oauth2.server.authorization;

import org.eulerframework.common.util.StringUtils;
import org.eulerframework.security.oauth2.core.oidc.EulerOidcScopes;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

        String username = null;
        Collection<? extends GrantedAuthority> authorities = null;
        if (authorization.getAttribute(Principal.class.getName()) instanceof UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
            if (usernamePasswordAuthenticationToken.getPrincipal() instanceof UserDetails userDetails) {
                if (scopes.contains(OidcScopes.PROFILE)) {
                    username = userDetails.getUsername();
                }
                if (scopes.contains(EulerOidcScopes.AUTHORITIES)) {
                    authorities = userDetails.getAuthorities();
                }
            } else {
                if (scopes.contains(EulerOidcScopes.AUTHORITIES)) {
                    authorities = usernamePasswordAuthenticationToken.getAuthorities();
                }
            }
        } else if (authorization.getAttribute(Principal.class.getName()) instanceof UserDetails userDetails) {
            if (scopes.contains(OidcScopes.PROFILE)) {
                username = userDetails.getUsername();
            }
            if (scopes.contains(EulerOidcScopes.AUTHORITIES)) {
                authorities = userDetails.getAuthorities();
            }
        }

        if (StringUtils.hasText(username)) {
            claims.put(StandardClaimNames.PREFERRED_USERNAME, username);
        }

        if (!CollectionUtils.isEmpty(authorities)) {
            claims.put("authorities", authorities);
        }
    }
}
