package net.eulerframework.web.module.authentication.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import net.eulerframework.web.core.base.entity.UUIDEntity;

//@Entity
@Table(name="SYS_URL_MATCHER")
public class UrlMatcher extends UUIDEntity<UrlMatcher> {

    @Column(name="URL_MATCHER",nullable=false,unique=true)
    private String urlMatcher;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="SYS_URL_AUTHORITY",
        joinColumns={@JoinColumn(name="URL_MATCHER_ID")},
        inverseJoinColumns={@JoinColumn(name="AUTHORITY_ID")})
    private Set<Authority> authorities;
    @Column(name="AUTH_ORDER")
    private Integer order;
    
    public String getUrlMatcher() {
        return urlMatcher;
    }
    public void setUrlMatcher(String urlMatcher) {
        this.urlMatcher = urlMatcher;
    }    
    public Set<Authority> getAuthorities() {
        return authorities;
    }
    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }
    public Integer getOrder() {
        return order;
    }
    public void setOrder(Integer order) {
        this.order = order;
    }
}
