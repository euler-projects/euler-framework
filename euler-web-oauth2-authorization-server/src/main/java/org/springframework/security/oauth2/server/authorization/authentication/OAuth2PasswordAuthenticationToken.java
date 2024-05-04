package org.springframework.security.oauth2.server.authorization.authentication;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Map;

public class OAuth2PasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    private final UsernamePasswordAuthenticationToken userPrincipal;

    protected OAuth2PasswordAuthenticationToken(
            UsernamePasswordAuthenticationToken userPrincipal,
            Authentication clientPrincipal,
            @Nullable Map<String, Object> additionalParameters) {
        super(AuthorizationGrantType.PASSWORD, clientPrincipal, additionalParameters);
        this.userPrincipal = userPrincipal;
    }

    public UsernamePasswordAuthenticationToken getUserPrincipal() {
        return userPrincipal;
    }
}
