/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2017 cFrost.sun (孙宾, SUN BIN) 
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
 * https://github.com/euler-projects/euler-framework
 * https://cfrost.net
 */
package net.eulerframework.web.module.authentication.entity;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.util.CollectionUtils;

import net.eulerframework.web.core.base.entity.UUIDEntity;

@Entity
@Table(name = "SYS_USER")
public class User extends UUIDEntity<User> implements EulerUserEntity {

    @Column(name = "USERNAME", nullable = false, unique = true)
    private String username;
    @Column(name = "EMAIL", unique = true)
    private String email;
    @Column(name = "MOBILE", unique = true)
    private String mobile;
    @Column(name = "PASSWORD", nullable = false)
    private String password;
    @Column(name = "FULL_NAME")
    private String fullName;
    @Column(name = "AVATAR", length=36)
    private String avatar;
    @Column(name = "ENABLED", nullable = false)
    private Boolean enabled;
    @Column(name = "ACCOUNT_NON_EXPIRED", nullable = false)
    private Boolean accountNonExpired;
    @Column(name = "ACCOUNT_NON_LOCKED", nullable = false)
    private Boolean accountNonLocked;
    @Column(name = "CREDENTIALS_NON_EXPIRED", nullable = false)
    private Boolean credentialsNonExpired;
    @Column(name = "ROOT")
    private Boolean root;
    @Column(name = "REGIST_TIME")
    private Date registTime;
    
    @Transient
    private Set<Authority> authorities;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name = "SYS_USER_GROUP", joinColumns = { @JoinColumn(name = "USER_ID") }, inverseJoinColumns = { @JoinColumn(name = "GROUP_ID") })
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Boolean getRoot() {
        return root;
    }

    public void setRoot(Boolean root) {
        this.root = root;
    }
    
}
