/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.web.module.oauth2.provider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

import org.eulerframework.web.module.oauth2.entity.EulerOAuth2ClientEntity;

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
    private boolean enabled;

    public EulerOAuth2ClientDetails(EulerOAuth2ClientEntity eulerOAuth2ClientEntity) {
        this.clientId = eulerOAuth2ClientEntity.getClientId();
        this.clientSecret = eulerOAuth2ClientEntity.getClientSecret();
        this.accessTokenValiditySeconds = eulerOAuth2ClientEntity.getAccessTokenValiditySeconds();
        this.refreshTokenValiditySeconds = eulerOAuth2ClientEntity.getRefreshTokenValiditySeconds();
        this.secretRequired = eulerOAuth2ClientEntity.getSecretRequired();
        this.scoped = eulerOAuth2ClientEntity.getIsScoped();
        this.additionalInformation = eulerOAuth2ClientEntity.getAdditionalInformation();
        this.authoritie = Optional.ofNullable(eulerOAuth2ClientEntity.getAuthorities()).orElse(new HashSet<>());
        this.registeredRedirectUri = eulerOAuth2ClientEntity.getRegisteredRedirectUri();
        this.resourceIds = eulerOAuth2ClientEntity.getResourceIds();
        this.neverNeedApprove = eulerOAuth2ClientEntity.getNeverNeedApprove();
        this.scope = eulerOAuth2ClientEntity.getScope();
        this.autoApproveScope = eulerOAuth2ClientEntity.getAutoApproveScope();
        this.authorizedGrantTypes = 
                Optional.ofNullable(eulerOAuth2ClientEntity.getAuthorizedGrantTypes())
                    .orElse(new HashSet<>())
                        .stream()
                        .map(authorizedGrantType -> authorizedGrantType.getValue())
                        .collect(Collectors.toSet());
        this.enabled = eulerOAuth2ClientEntity.getEnabled();
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

    /**
     * @return
     */
    public boolean isEnabled() {
        return this.enabled;
    }

}
