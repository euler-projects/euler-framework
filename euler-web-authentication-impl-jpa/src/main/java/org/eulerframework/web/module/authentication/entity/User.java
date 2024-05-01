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
package org.eulerframework.web.module.authentication.entity;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.springframework.util.CollectionUtils;

import org.eulerframework.data.entity.AuditingUUIDEntity;

@Entity
@Table(name = "sys_user")
public class User extends AuditingUUIDEntity implements EulerUserEntity {

    @Column(name = "username", nullable = false, unique = true)
    private String username;
    @Column(name = "email", unique = true)
    private String email;
    @Column(name = "mobile", unique = true)
    private String mobile;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;
    @Column(name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired;
    @Column(name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked;
    @Column(name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired;
    @Column(name = "root")
    private Boolean root;
    @Column(name = "regist_time")
    private Date registTime;
    
    @Transient
    private Set<Authority> authorities;
    
    @ManyToMany(fetch = FetchType.EAGER)
    //@Fetch(FetchMode.SELECT)
    @JoinTable(name = "sys_user_group", joinColumns = {
            @JoinColumn(name = "user_id")
    }, inverseJoinColumns = {
            @JoinColumn(name = "group_id")
    })
    private Set<Group> groups;

    @Override
    public Boolean isRoot() {
        return this.root;
    }

    @Override
    public Collection<Authority> getAuthorities() {
        Collection<Authority> result =  new HashSet<>();
        
        if(!CollectionUtils.isEmpty(this.groups)) {
            result.addAll(this.groups
                    .stream()
                    .flatMap(group -> group.getAuthorities().stream())
                    .collect(Collectors.toSet()));            
        }        
        
        if(!CollectionUtils.isEmpty(this.authorities)) {
            result.addAll(this.authorities);
        }
        
        return result;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public Boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public Boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public Boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public Boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void eraseCredentials() {
        this.password = "";
    }

    @Override
    public String getUserId() {
        return this.getId();
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getMobile() {
        return mobile;
    }

    @Override
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void setAccountNonExpired(Boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    @Override
    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    @Override
    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @Override
    public void setRegistTime(Date registTime) {
        this.registTime = registTime;
    }

    @Override
    public Date getRegistTime() {
        return this.registTime;
    }

    public Set<Group> getGroups() {
        return groups;
    }

    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }
}
