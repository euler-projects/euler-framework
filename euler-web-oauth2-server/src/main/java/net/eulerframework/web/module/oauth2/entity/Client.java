package net.eulerframework.web.module.oauth2.entity;

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
import javax.validation.constraints.NotNull;

import net.eulerframework.web.core.base.entity.UUIDEntity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

@Entity
@Table(name = "SYS_CLIENT")
public class Client extends UUIDEntity<Client> implements ClientDetails {
    
    private final static Set<GrantedAuthority> DEFAULT_GRANTED_AUTHORITY = new HashSet<>();

    @NotNull
    @Column(name= "CLIENT_ID", nullable = false, unique = true)
    private String clientId;
    
    @Column(name = "CLIENT_SECRET", nullable = false)
    private String clientSecret;
    
    @Column(name = "ENABLED", nullable = false)
    private Boolean enabled;

    @Column(name = "ACCESS_TOLEN_LIFE", nullable = false)
    private Integer accessTokenValiditySeconds;

    @Column(name = "REFRESH_TOKEN_LIFE", nullable = false)
    private Integer refreshTokenValiditySeconds;

    @Column(name = "NEVER_NEED_APPROVE", nullable = false)
    private Boolean neverNeedApprove;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "SYS_CLIENT_RESOURCE", joinColumns = { @JoinColumn(name = "CLIENT_ID") }, inverseJoinColumns = { @JoinColumn(name = "RESOURCE_ID") })
    private Set<Resource> resources;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "SYS_CLIENT_SCOPE", joinColumns = { @JoinColumn(name = "CLIENT_ID") }, inverseJoinColumns = { @JoinColumn(name = "SCOPE_ID") })
    private Set<Scope> scopes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "SYS_CLIENT_GRANT_TYPE", joinColumns = { @JoinColumn(name = "CLIENT_ID", referencedColumnName = "ID") })
    @Column(name = "GRANT_TYPE")
    private Set<String> authorizedGrantTypes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "SYS_CLIENT_REDIRECT_URI", joinColumns = { @JoinColumn(name = "CLIENT_ID", referencedColumnName = "ID") })
    @Column(name = "REDIRECT_URI")
    private Set<String> registeredRedirectUri;
    
    @Column(name="DESCRIPTION")
    private String description;

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

    public void setScopes(Set<Scope> scopes) {
        this.scopes = scopes;
    }


    public void setRegisteredRedirectUri(Set<String> registeredRedirectUri) {
        this.registeredRedirectUri = registeredRedirectUri;
    }

    public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    public void setNeverNeedApprove(Boolean neverNeedApprove) {
        this.neverNeedApprove = neverNeedApprove;
    }

    public Boolean getNeverNeedApprove() {
        return neverNeedApprove;
    }

    public Set<Resource> getResources() {
        return resources;
    }

    public Set<Scope> getScopes() {
        return scopes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getClientId() {
        return this.clientId;
    }

    @Override
    public String getClientSecret() {
        return this.clientSecret;
    }

    @Override
    public Set<String> getResourceIds() {
        Set<String> result = new HashSet<>();
        for (Resource resource : this.resources) {
            result.add(resource.getResourceId());
        }
        return result;
    }

    @Override
    public Set<String> getScope() {
        Set<String> result = new HashSet<>();
        for (Scope scope : this.scopes) {
            result.add(scope.getScope());
        }
        return result;
    }

    @Override
    public Set<GrantedAuthority> getAuthorities() {
        return DEFAULT_GRANTED_AUTHORITY;
    }

    public void setAuthorizedGrantTypes(Set<GrantType> grantTypes) {
        if (grantTypes == null) {
            this.authorizedGrantTypes = null;
        }
        Set<String> authorizedGrantTypes = new HashSet<>();
        for(GrantType grantType : grantTypes){
            authorizedGrantTypes.add(grantType.toString());
        }
        this.authorizedGrantTypes = authorizedGrantTypes;
    }

    @Override
    @Transient
    public Set<String> getAuthorizedGrantTypes() {
        return this.authorizedGrantTypes;
    }

    @Override
    public Set<String> getRegisteredRedirectUri() {
        return this.registeredRedirectUri;
    }

    @Override
    public Integer getAccessTokenValiditySeconds() {
        return this.accessTokenValiditySeconds;
    }

    @Override
    public Integer getRefreshTokenValiditySeconds() {
        return this.refreshTokenValiditySeconds;
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
    @Transient
    public boolean isAutoApprove(String scope) {
        return this.neverNeedApprove;
    }

}
