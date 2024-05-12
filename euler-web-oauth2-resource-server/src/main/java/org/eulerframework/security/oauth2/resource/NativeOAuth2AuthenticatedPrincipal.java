package org.eulerframework.security.oauth2.resource;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class NativeOAuth2AuthenticatedPrincipal implements OAuth2AuthenticatedPrincipal, Serializable {

    private final Map<String, Object> attributes;

    private final Collection<GrantedAuthority> authorities;

    private final Collection<GrantedAuthority> resourceOwnerAuthorities;

    private final String name;

    /**
     * Constructs an {@code NativeOAuth2AuthenticatedPrincipal} using the provided
     * parameters.
     *
     * @param attributes  the attributes of the OAuth 2.0 token
     * @param authorities the authorities of the OAuth 2.0 token
     */
    public NativeOAuth2AuthenticatedPrincipal(Map<String, Object> attributes,
                                              Collection<GrantedAuthority> authorities,
                                              Collection<GrantedAuthority> resourceOwnerAuthorities) {
        this(null, attributes, authorities, resourceOwnerAuthorities);
    }

    /**
     * Constructs an {@code NativeOAuth2AuthenticatedPrincipal} using the provided
     * parameters.
     *
     * @param name        the name attached to the OAuth 2.0 token
     * @param attributes  the attributes of the OAuth 2.0 token
     * @param authorities the authorities of the OAuth 2.0 token
     */
    public NativeOAuth2AuthenticatedPrincipal(String name, Map<String, Object> attributes,
                                              Collection<GrantedAuthority> authorities,
                                              Collection<GrantedAuthority> resourceOwnerAuthorities) {
        Assert.notEmpty(attributes, "attributes cannot be empty");
        this.attributes = Collections.unmodifiableMap(attributes);
        this.authorities = (authorities != null) ? Collections.unmodifiableCollection(authorities)
                : AuthorityUtils.NO_AUTHORITIES;
        this.resourceOwnerAuthorities = (resourceOwnerAuthorities != null) ? Collections.unmodifiableCollection(resourceOwnerAuthorities)
                : AuthorityUtils.NO_AUTHORITIES;
        this.name = (name != null) ? name : (String) this.attributes.get("sub");
    }

    /**
     * Gets the attributes of the OAuth 2.0 token in map form.
     *
     * @return a {@link Map} of the attribute's objects keyed by the attribute's names
     */
    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }


    public Collection<? extends GrantedAuthority> getResourceOwnerAuthorities() {
        return this.resourceOwnerAuthorities;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
