package org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

public class OAuth2ConfigurerUtilsAccessor {
    public static OAuth2AuthorizationService getAuthorizationService(HttpSecurity http) {
        return OAuth2ConfigurerUtils.getAuthorizationService(http);
    }

    public static OAuth2TokenGenerator<? extends OAuth2Token> getTokenGenerator(HttpSecurity http) {
        return OAuth2ConfigurerUtils.getTokenGenerator(http);
    }
}
