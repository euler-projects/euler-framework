/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
