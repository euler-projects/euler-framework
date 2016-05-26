package net.eulerform.web.core.security.authentication.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import net.eulerform.web.core.base.entity.UUIDEntity;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.UserDetails;

@SuppressWarnings("serial")
@Entity
@XmlRootElement
@Table(name = "SYS_USER")
public class User extends UUIDEntity<User> implements UserDetails, CredentialsContainer {

    @Column(name = "USERNAME", nullable = false, unique = true)
    private String username;
    @Column(name = "PASSWORD", nullable = false)
    private String password;
    @Column(name = "ENABLED", nullable = false)
    private Boolean enabled;
    @Column(name = "ACCOUNT_NON_EXPIRED", nullable = false)
    private Boolean accountNonExpired;
    @Column(name = "ACCOUNT_NON_LOCKED", nullable = false)
    private Boolean accountNonLocked;
    @Column(name = "CREDENTIALS_NON_EXPIRED", nullable = false)
    private Boolean credentialsNonExpired;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "SYS_USER_AUTHORITY", joinColumns = { @JoinColumn(name = "USER_ID") }, inverseJoinColumns = { @JoinColumn(name = "AUTHORITY_ID") })
    private Set<Authority> authorities;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return this.enabled == null ? false : this.enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAccountNonExpired() {
        return this.accountNonExpired == null ? false : this.accountNonExpired;
    }

    public void setAccountNonExpired(Boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public boolean isAccountNonLocked() {
        return this.accountNonLocked == null ? false : this.accountNonLocked;
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired == null ? false : this.credentialsNonExpired;
    }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }
    
    @Override
    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

}
