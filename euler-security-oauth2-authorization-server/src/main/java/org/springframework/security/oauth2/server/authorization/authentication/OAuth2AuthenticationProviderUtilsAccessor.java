package org.springframework.security.oauth2.server.authorization.authentication;

import org.springframework.security.core.Authentication;

public class OAuth2AuthenticationProviderUtilsAccessor {
    public static OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(Authentication authentication) {
        return OAuth2AuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient(authentication);
    }
}
