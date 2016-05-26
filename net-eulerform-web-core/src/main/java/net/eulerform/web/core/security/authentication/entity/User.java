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
@Table(name="SYS_USER")
public class User extends UUIDEntity<User> implements UserDetails, CredentialsContainer {

    @Column(name="USERNAME",nullable=false,unique=true)
    private String username;
    @Column(name="PASSWORD",nullable=false)
    private String password;
    @Column(name="ENABLED",nullable=false)
    private Boolean enabled;
    @Column(name="ACCOUNT_NON_EXPIRED",nullable=false)
    private Boolean accountNonExpired;
    @Column(name="ACCOUNT_NON_LOCKED",nullable=false)
    private Boolean accountNonLocked;
    @Column(name="CREDENTIALS_NON_EXPIRED",nullable=false)
    private Boolean credentialsNonExpired;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="SYS_USER_AUTHORITY",
        joinColumns={@JoinColumn(name="USER_ID")},
        inverseJoinColumns={@JoinColumn(name="AUTHORITY_ID")})
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
        return enabled;
    }
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    public void setAccountNonExpired(Boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }
    //@XmlTransient
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
    
//    public User() {
//        super();
//    }
//    
//    public User(String username, String password, Collection<Authority> authorities) {
//        this(username, password, true, true, true, true, authorities);
//    }
//
//    public User(String username, String password, Boolean enabled, Boolean accountNonExpired, Boolean credentialsNonExpired, Boolean accountNonLocked,
//            Collection<Authority> authorities) {
//        super();
//        if ((username == null) || ("".equals(username)) || (password == null)) {
//            throw new IllegalArgumentException("Cannot pass null or empty values to constructor");
//        }
//
//        this.username = username;
//        this.password = password;
//        this.enabled = enabled;
//        this.accountNonExpired = accountNonExpired;
//        this.credentialsNonExpired = credentialsNonExpired;
//        this.accountNonLocked = accountNonLocked;
//        this.authority = Collections.unmodifiableSet(sortAuthorities(authorities));
//    }
//    
//    private static <T extends GrantedAuthority> SortedSet<T> sortAuthorities(Collection<T> authorities) {
//        Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection");
//
//        SortedSet<T> sortedAuthorities = new TreeSet<T>(new AuthorityComparator());
//
//        for (T grantedAuthority : authorities) {
//            Assert.notNull(grantedAuthority, "GrantedAuthority list cannot contain any null elements");
//
//            sortedAuthorities.add(grantedAuthority);
//        }
//
//        return sortedAuthorities;
//    }
//    
//    private static class AuthorityComparator implements Comparator<GrantedAuthority>, Serializable{
//
//        public int compare(GrantedAuthority g1, GrantedAuthority g2) {
//            if (g2.getAuthority() == null) {
//                return -1;
//            }
//
//            if (g1.getAuthority() == null) {
//                return 1;
//            }
//
//            return g1.getAuthority().compareTo(g2.getAuthority());
//        }
//    }

}
