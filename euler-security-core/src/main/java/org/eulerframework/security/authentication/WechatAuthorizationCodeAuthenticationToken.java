package org.eulerframework.security.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * An {@link org.springframework.security.core.Authentication} implementation that is
 * designed for simple presentation of a WeChat Login Code.
 */
public class WechatAuthorizationCodeAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;

    private Object credentials;

    /**
     * This constructor can be safely used by any code that wishes to create a
     * <code>WechatAuthorizationCodeAuthenticationToken</code>, as the {@link #isAuthenticated()}
     * will return <code>false</code>.
     */
    public WechatAuthorizationCodeAuthenticationToken(Object credentials) {
        super(null);
        this.principal = null;
        this.credentials = credentials;
        setAuthenticated(false);
    }

    /**
     * This constructor should only be used by <code>AuthenticationManager</code> or
     * <code>AuthenticationProvider</code> implementations that are satisfied with
     * producing a trusted (i.e. {@link #isAuthenticated()} = <code>true</code>)
     * authentication token.
     */
    public WechatAuthorizationCodeAuthenticationToken(Object principal, Object credentials,
                                                      Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true); // must use super, as we override
    }

    /**
     * This factory method can be safely used by any code that wishes to create an
     * unauthenticated <code>WechatAuthorizationCodeAuthenticationToken</code>.
     */
    public static WechatAuthorizationCodeAuthenticationToken unauthenticated(Object credentials) {
        return new WechatAuthorizationCodeAuthenticationToken(credentials);
    }

    /**
     * This factory method can be safely used by any code that wishes to create an
     * authenticated <code>WechatAuthorizationCodeAuthenticationToken</code>.
     */
    public static WechatAuthorizationCodeAuthenticationToken authenticated(Object principal, Object credentials,
                                                                           Collection<? extends GrantedAuthority> authorities) {
        return new WechatAuthorizationCodeAuthenticationToken(principal, credentials, authorities);
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated,
                "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }

}
