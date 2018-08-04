package net.eulerframework.web.module.oauth2.provider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

import net.eulerframework.web.module.oauth2.entity.EulerOauth2ClientEntity;

public class EulerOAuth2ClientDetails implements ClientDetails {

    private String clientId;
    private String clientSecret;
    private int accessTokenValiditySeconds;
    private int refreshTokenValiditySeconds;
    private boolean secretRequired;
    private boolean scoped;
    private Map<String, Object> additionalInformation;
    private Collection<GrantedAuthority> authoritie;
    private Set<String> registeredRedirectUri;
    private Set<String> resourceIds;
    private Set<String> scope;
    private boolean neverNeedApprove;
    private Set<String> autoApproveScope;
    private Set<String> authorizedGrantTypes;

    public EulerOAuth2ClientDetails(EulerOauth2ClientEntity eulerOauth2ClientEntity) {
        this.clientId = eulerOauth2ClientEntity.getClientId();
        this.clientSecret = eulerOauth2ClientEntity.getClientSecret();
        this.accessTokenValiditySeconds = eulerOauth2ClientEntity.getAccessTokenValiditySeconds();
        this.refreshTokenValiditySeconds = eulerOauth2ClientEntity.getRefreshTokenValiditySeconds();
        this.secretRequired = eulerOauth2ClientEntity.getSecretRequired();
        this.scoped = eulerOauth2ClientEntity.getIsScoped();
        this.additionalInformation = eulerOauth2ClientEntity.getAdditionalInformation();
        this.authoritie = Optional.ofNullable(eulerOauth2ClientEntity.getAuthorities()).orElse(new HashSet<>());
        this.registeredRedirectUri = eulerOauth2ClientEntity.getRegisteredRedirectUri();
        this.resourceIds = eulerOauth2ClientEntity.getResourceIds();
        this.neverNeedApprove = eulerOauth2ClientEntity.getNeverNeedApprove();
        this.scope = eulerOauth2ClientEntity.getScope();
        this.autoApproveScope = eulerOauth2ClientEntity.getAutoApproveScope();
        this.authorizedGrantTypes = 
                Optional.ofNullable(eulerOauth2ClientEntity.getAuthorizedGrantTypes())
                    .orElse(new HashSet<>())
                        .stream()
                        .map(authorizedGrantType -> authorizedGrantType.getValue())
                        .collect(Collectors.toSet());
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public Integer getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    @Override
    public Integer getRefreshTokenValiditySeconds() {
        return refreshTokenValiditySeconds;
    }

    @Override
    public boolean isSecretRequired() {
        return secretRequired;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authoritie;
    }

    @Override
    public Map<String, Object> getAdditionalInformation() {
        return additionalInformation;
    }

    @Override
    public Set<String> getAuthorizedGrantTypes() {
        return authorizedGrantTypes;
    }

    @Override
    public Set<String> getRegisteredRedirectUri() {
        return registeredRedirectUri;
    }

    @Override
    public Set<String> getResourceIds() {
        return resourceIds;
    }

    @Override
    public Set<String> getScope() {
        return scope;
    }

    @Override
    public boolean isAutoApprove(String scope) {
        return neverNeedApprove || (this.scope.contains(scope) && this.autoApproveScope.contains(scope));
    }

    @Override
    public boolean isScoped() {
        return scoped;
    }

}
