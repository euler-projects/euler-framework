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

    /**
     * Unwrap the resource owner behind the value a grant provider stored as the principal, either
     * under an authorization's {@code java.security.Principal} attribute or under a token context's
     * principal key.
     * <p>
     * Grant providers do not agree on what they store there: some a bare {@link UserDetails}, most an
     * {@link Authentication} of their own type carrying one as its principal. Callers have to match
     * on that shape rather than enumerate token types, or any grant they did not list silently
     * yields nothing.
     *
     * @param principal the stored value, possibly {@code null}
     * @return the resource owner, or {@code null} when the request has none, as with the
     * client_credentials grant
     */
    public static UserDetails resolveUserDetails(Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return userDetails;
        }
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails;
        }
        return null;
    }

    public static void putExtendClaims(OAuth2Authorization authorization, Set<String> scopes, Map<String, Object> claims) {
        if (scopes == null || scopes.isEmpty()) {
            return;
        }

        if(!scopes.contains(EulerOidcScopes.AUTHORITIES)
                && !scopes.contains(OidcScopes.PROFILE)) {
            return;
        }

        Object principalAttribute = authorization.getAttribute(Principal.class.getName());
        UserDetails userDetails = resolveUserDetails(principalAttribute);

        // An Authentication whose principal is not a UserDetails still vouches for the authorities
        // it was issued with, which is all there is to report in that case.
        Collection<? extends GrantedAuthority> tokenAuthorities =
                userDetails == null && principalAttribute instanceof Authentication authentication
                        ? authentication.getAuthorities()
                        : null;

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
