package net.eulerform.web.core.security.authentication.entity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.eulerform.web.core.base.entity.UUIDEntity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

@SuppressWarnings("serial")
@Entity
@Table(name="SYS_CLIENT")
public class Client extends UUIDEntity<Client> implements ClientDetails {
    
    private static final Set<String> AUTHORIZDE_GRANT_TYPES = new HashSet<>();
    static {
        AUTHORIZDE_GRANT_TYPES.add("authorization_code");
        AUTHORIZDE_GRANT_TYPES.add("refresh_token");
    }

    @Column(name = "CLIENT_SECRET")
    private String clientSecret;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "SYS_CLIENT_RESOURCE", joinColumns = { @JoinColumn(name = "CLIENT_ID") }, inverseJoinColumns = { @JoinColumn(name = "RESOURCE_ID") })
    private Set<Resource> resources;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "SYS_CLIENT_SCOPE", joinColumns = { @JoinColumn(name = "CLIENT_ID") }, inverseJoinColumns = { @JoinColumn(name = "SCOPE_ID") })
    private Set<Scope> scopes;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "SYS_CLIENT_AUTHORITY", joinColumns = { @JoinColumn(name = "CLIENT_ID") }, inverseJoinColumns = { @JoinColumn(name = "AUTHORITY_ID") })
    private Set<Authority> authorities;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "SYS_CLIENT_REDIRECT_URI", joinColumns = { @JoinColumn(name = "CLIENT_ID", referencedColumnName = "ID") })
    @Column(name = "REDIRECT_URI")
    private Set<String> registeredRedirectUri;

    @Override
    public String getClientId() {
        return super.getId();
    }

    @Override
    public String getClientSecret() {
        return this.clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

    @Override
    public Set<String> getResourceIds() {
        Set<String> result = new HashSet<>();
        for(Resource resource : this.resources){
            result.add(resource.getResourceName());
        }
        return result;
    }

    @Override
    public Set<String> getScope() {
        Set<String> result = new HashSet<>();
        for(Scope scope : this.scopes){
            result.add(scope.getScope());
        }
        return result;
    }

    public void setScopes(Set<Scope> scopes) {
        this.scopes = scopes;
    }

    @Override
    public Set<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> result = new HashSet<>();
        result.addAll(this.authorities);
        return result;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public Set<String> getRegisteredRedirectUri() {
        return this.registeredRedirectUri;
    }

    public void setRegisteredRedirectUri(Set<String> registeredRedirectUri) {
        this.registeredRedirectUri = registeredRedirectUri;
    }

    @Override
    @Transient
    public Set<String> getAuthorizedGrantTypes() {
        return AUTHORIZDE_GRANT_TYPES;
    }

    @Override
    @Transient
    public Integer getAccessTokenValiditySeconds() {
        return 3600;
    }

    @Override
    @Transient
    public Integer getRefreshTokenValiditySeconds() {
        return -1;
    }

    @Override
    @Transient
    public Map<String, Object> getAdditionalInformation() {
        return null;
    }

    @Override
    @Transient
    public boolean isSecretRequired() {
        return true;
    }

    @Override
    @Transient
    public boolean isScoped() {
        return true;
    }

    @Override
    public boolean isAutoApprove(String scope) {
        System.out.println("!!!!!!!isAutoApprove+"+scope);
        // TODO Auto-generated method stub
        return true;
    }

}
