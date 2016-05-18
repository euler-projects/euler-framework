package net.eulerform.web.core.security.authentication.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;

import net.eulerform.web.core.base.entity.UUIDEntity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

@SuppressWarnings("serial")
public class Client extends UUIDEntity<Client> implements ClientDetails {

    @Column(name = "CLIENT_ID")
    private String clientId;
    
    @Column(name = "CLIENT_SECRET")
    private String clientSecret;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "SYS_CLIENT_SCOPE", joinColumns = {
            @JoinColumn(name = "CLIENT_ID",
                    referencedColumnName = "ID")
    })
    @Column(name = "SCOPE")
    private Set<String> scope;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "SYS_CLIENT_GRANT", joinColumns = {
            @JoinColumn(name = "CLIENT_ID",
                    referencedColumnName = "ID")
    })
    @Column(name = "GRANT_NAME")
    private Set<String> authorizedGrantTypes;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "SYS_CLIENT_REDIRECTURI", joinColumns = {
            @JoinColumn(name = "CLIENT_ID",
                    referencedColumnName = "ID")
    })
    @Column(name = "URI")
    private Set<String> registeredRedirectUri;

    @Override
    public String getClientId()
    {
        return this.clientId;
    }

    public void setClientId(String clientId)
    {
        this.clientId = clientId;
    }

    @Override
    public String getClientSecret()
    {
        return this.clientSecret;
    }

    public void setClientSecret(String clientSecret)
    {
        this.clientSecret = clientSecret;
    }

    @Override
    public Set<String> getScope()
    {
        return this.scope;
    }

    public void setScope(Set<String> scope)
    {
        this.scope = scope;
    }

    @Override
    public Set<String> getAuthorizedGrantTypes()
    {
        return this.authorizedGrantTypes;
    }

    public void setAuthorizedGrantTypes(Set<String> authorizedGrantTypes)
    {
        this.authorizedGrantTypes = authorizedGrantTypes;
    }

    @Override
    public Set<String> getRegisteredRedirectUri()
    {
        return this.registeredRedirectUri;
    }

    public void setRegisteredRedirectUri(Set<String> registeredRedirectUri)
    {
        this.registeredRedirectUri = registeredRedirectUri;
    }

    private static final Set<String> RESOURCE_IDS = new HashSet<>();
    private static final Set<GrantedAuthority> AUTHORITIES = new HashSet<>();
    static {
        RESOURCE_IDS.add("SUPPORT");
        AUTHORITIES.add(new SimpleGrantedAuthority("OAUTH_CLIENT"));
    }

    @Override
    @Transient
    public Set<String> getResourceIds()
    {
        return RESOURCE_IDS;
    }

    @Override
    @Transient
    public Collection<GrantedAuthority> getAuthorities()
    {
        return AUTHORITIES;
    }

    @Override
    @Transient
    public Integer getAccessTokenValiditySeconds()
    {
        return 3600;
    }

    @Override
    @Transient
    public Integer getRefreshTokenValiditySeconds()
    {
        return -1;
    }

    @Override
    @Transient
    public Map<String, Object> getAdditionalInformation()
    {
        return null;
    }

    @Override
    @Transient
    public boolean isSecretRequired()
    {
        return true;
    }

    @Override
    @Transient
    public boolean isScoped()
    {
        return true;
    }

    @Override
    public boolean isAutoApprove(String scope) {
        // TODO Auto-generated method stub
        return false;
    }

}
