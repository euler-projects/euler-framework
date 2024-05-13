package org.springframework.security.oauth2.core.context;

import org.eulerframework.security.core.context.PrincipalUserContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;

public class OAuth2AuthenticatedPrincipalUserContext extends PrincipalUserContext {

    @Override
    protected UserDetails getUserDetails(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        OAuth2AuthenticatedPrincipal authenticatedPrincipal = (OAuth2AuthenticatedPrincipal) principal;
        String sub = authenticatedPrincipal.getAttribute(OAuth2TokenIntrospectionClaimNames.SUB);
        return null;
//        Object subDetails = authenticatedPrincipal.getAttribute(EulerOAuth2TokenIntrospectionClaimNames.SUB_DETAILS);
//
//        if (subDetails instanceof UserDetails) {
//            return (UserDetails) subDetails;
//        }
//
//        Map<String, Object> map;
//        try {
//            map = (Map<String, Object>) subDetails;
//        } catch (ClassCastException e) {
//            return null;
//        }
//
//        if (map == null) {
//            return null;
//        }
//
//        EulerUserDetails userDetails = new EulerUserDetails();
//        userDetails.setUserId(Optional.ofNullable(map.get("userId")).map(String::valueOf).orElse(null));
//        userDetails.setUsername(Optional.ofNullable(map.get("username")).map(String::valueOf).orElse(null));
//        userDetails.setEnabled(Optional.ofNullable(map.get("enabled")).map(String::valueOf).map(Boolean::parseBoolean).orElse(false));
//        userDetails.setAccountNonLocked(Optional.ofNullable(map.get("accountNonLocked")).map(String::valueOf).map(Boolean::parseBoolean).orElse(false));
//        userDetails.setAccountNonExpired(Optional.ofNullable(map.get("accountNonExpired")).map(String::valueOf).map(Boolean::parseBoolean).orElse(false));
//        userDetails.setCredentialsNonExpired(Optional.ofNullable(map.get("credentialsNonExpired")).map(String::valueOf).map(Boolean::parseBoolean).orElse(false));
//        userDetails.setAuthorities(Optional.ofNullable(map.get("authorities"))
//                .map(authorities -> (Collection<Object>) authorities)
//                .map(authorities -> authorities.stream()
//                        .map(authority -> (Map<String, Object>) authority)
//                        .map(authority -> authority.get("authority"))
//                        .map(String::valueOf)
//                        .map(SimpleGrantedAuthority::new)
//                        .collect(Collectors.toList())
//                )
//                .orElse(Collections.emptyList())
//        );
//        return userDetails;
    }
}
