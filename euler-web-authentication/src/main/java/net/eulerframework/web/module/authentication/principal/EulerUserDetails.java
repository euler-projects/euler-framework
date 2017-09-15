/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2017 cFrost.sun(孙宾, SUN BIN) 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following website
 * 
 * https://eulerproject.io
 * https://github.com/euler-form/web-form
 * https://cfrost.net
 */
package net.eulerframework.web.module.authentication.principal;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import net.eulerframework.web.module.authentication.entity.EulerAuthorityEntity;
import net.eulerframework.web.module.authentication.entity.EulerUserEntity;

/**
 * @author cFrost
 *
 */
public final class EulerUserDetails implements UserDetails, CredentialsContainer {
    public final static UUID ROOT_USER_ID = new UUID(0, 0);
    public final static String ROOT_USERNAME = "root";
    public final static SimpleGrantedAuthority ROOT_AUTHORITY = new SimpleGrantedAuthority("ROOT");
    
    private UUID userId;
    private String username;
    private String password;
    private Collection<SimpleGrantedAuthority> authorities;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    public EulerUserDetails(EulerUserEntity userEntity) {
        Assert.hasText(userEntity.getUsername(), "Username can not be null");
        Assert.hasText(userEntity.getPassword(), "Password can not be null");
        
        this.userId = UUID.fromString(userEntity.getUserId());
        this.username = userEntity.getUsername();
        this.password = userEntity.getPassword();
        
        this.accountNonExpired = userEntity.isAccountNonExpired() == null ? false : userEntity.isAccountNonExpired();
        this.accountNonLocked = userEntity.isAccountNonLocked() == null ? false : userEntity.isAccountNonLocked();
        this.credentialsNonExpired = userEntity.isCredentialsNonExpired() == null ? false : userEntity.isCredentialsNonExpired();
        this.enabled = userEntity.isEnabled() == null ? false : userEntity.isEnabled();
        
        Collection<? extends EulerAuthorityEntity> authorities = userEntity.getAuthorities();
        this.authorities = 
                CollectionUtils.isEmpty(authorities) ? new HashSet<>() : authorities
                    .stream()
                    .map(authority -> authority.toSimpleGrantedAuthority())
                    .collect(Collectors.toSet());

        if (userEntity.isRoot() != null && userEntity.isRoot()) {
            this.authorities .add(ROOT_AUTHORITY);
        }
    }

    /**
     * Returns the id of the user.
     * 
     * @return the id of the user
     */
    public UUID getUserId() {
        return this.userId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<SimpleGrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void eraseCredentials() {
        this.password = "";
    }
    
}
