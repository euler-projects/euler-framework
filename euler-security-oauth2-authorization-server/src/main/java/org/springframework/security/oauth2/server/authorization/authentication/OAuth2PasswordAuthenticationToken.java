package org.springframework.security.oauth2.server.authorization.authentication;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OAuth2PasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    private final UsernamePasswordAuthenticationToken userPrincipal;
    private final Set<String> scopes;

    public OAuth2PasswordAuthenticationToken(
            UsernamePasswordAuthenticationToken userPrincipal,
            Authentication clientPrincipal,
            @Nullable Set<String> scopes,
            @Nullable Map<String, Object> additionalParameters) {
        super(AuthorizationGrantType.PASSWORD, clientPrincipal, additionalParameters);
        this.userPrincipal = userPrincipal;
        this.scopes = Collections.unmodifiableSet(
                scopes != null ?
                        new HashSet<>(scopes) :
                        Collections.emptySet());
    }

    public UsernamePasswordAuthenticationToken getUserPrincipal() {
        return userPrincipal;
    }

    public Set<String> getScopes() {
        return scopes;
    }
}
