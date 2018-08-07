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
package net.eulerframework.web.module.oauth2.entity;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;

import net.eulerframework.web.module.oauth2.enums.GrantType;
import net.eulerframework.web.module.oauth2.provider.EulerOAuth2ClientDetails;

/**
 * @author cFrost
 *
 */
public interface EulerOAuth2ClientEntity extends CredentialsContainer {
    /**
     * The client id.
     * 
     * @return The client id.
     */
    String getClientId();

    /**
     * The resources that this client can access. Can be ignored by callers if empty.
     * 
     * @return The resources of this client.
     */
    Set<String> getResourceIds();

    /**
     * Whether a secret is required to authenticate this client.
     * 
     * @return Whether a secret is required to authenticate this client.
     */
    Boolean getSecretRequired();

    /**
     * The client secret. Ignored if the {@link #getSecretRequired() secret isn't required}.
     * 
     * @return The client secret.
     */
    String getClientSecret();

    /**
     * Whether this client is limited to a specific scope. If false, the scope of the authentication request will be
     * ignored.
     * 
     * @return Whether this client is limited to a specific scope.
     */
    Boolean getIsScoped();

    /**
     * The scope of this client. Empty if the client isn't scoped.
     * 
     * @return The scope of this client.
     */
    Set<String> getScope();

    /**
     * The grant types for which this client is authorized.
     * 
     * @return The grant types for which this client is authorized.
     */
    Set<GrantType> getAuthorizedGrantTypes();

    /**
     * The pre-defined redirect URI for this client to use during the "authorization_code" access grant. See OAuth spec,
     * section 4.1.1.
     * 
     * @return The pre-defined redirect URI for this client.
     */
    Set<String> getRegisteredRedirectUri();

    /**
     * Returns the authorities that are granted to the OAuth client.
     * Note that these are NOT the authorities that are granted to the user with an authorized access token.
     * Instead, these authorities are inherent to the client itself.
     * 
     * @return the authorities
     */
    Collection<GrantedAuthority> getAuthorities();

    /**
     * The access token validity period for this client. Null if not set explicitly (implementations might use that fact
     * to provide a default value for instance).
     * 
     * @return the access token validity period
     */
    Integer getAccessTokenValiditySeconds();

    /**
     * The refresh token validity period for this client. Null for default value set by token service, and 
     * zero or negative for non-expiring tokens.
     * 
     * @return the refresh token validity period
     */
    Integer getRefreshTokenValiditySeconds();

    /**
     * Additional information for this client, not needed by the vanilla OAuth protocol but might be useful, for example,
     * for storing descriptive information.
     * 
     * @return a map of additional information
     */
    Map<String, Object> getAdditionalInformation();

    /**
     * 此Client永远不需要用户批准，当此字段返回<code>true</code>时, <code>AutoApproveScope</code>会被忽略
     * @return 永远不需要用户批准返回<code>true</code>
     */
    Boolean getNeverNeedApprove();

    /**
     * 不需要用户批准的scope
     * @return 不需要用户批准的scope
     */
    Set<String> getAutoApproveScope();

    /**
     * @return
     */
    Boolean getEnabled();

    /**
     * 
     */
    default EulerOAuth2ClientDetails toEulerOAuth2ClientDetails() {
        return new EulerOAuth2ClientDetails(this);
    }
}
