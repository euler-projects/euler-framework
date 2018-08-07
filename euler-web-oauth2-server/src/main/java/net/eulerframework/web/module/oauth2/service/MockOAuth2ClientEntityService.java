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
package net.eulerframework.web.module.oauth2.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.eulerframework.web.module.oauth2.entity.EulerOAuth2ClientEntity;
import net.eulerframework.web.module.oauth2.enums.GrantType;

/**
 * @author cFrost
 *
 */
public class MockOAuth2ClientEntityService implements EulerOAuth2ClientEntityService {
    
    private DefaultOAuth2ClientEntity defaultOAuth2ClientEntity = new DefaultOAuth2ClientEntity();

    public MockOAuth2ClientEntityService(PasswordEncoder passwordEncoder) {
        defaultOAuth2ClientEntity.setClientSecret(passwordEncoder.encode("default"));
    }
    
    @Override
    public EulerOAuth2ClientEntity loadClientById(String clientId) {
        if(defaultOAuth2ClientEntity.getClientId().equals(clientId)) {
            return defaultOAuth2ClientEntity;
        }
        
        return null;
    }
    
    public class DefaultOAuth2ClientEntity implements EulerOAuth2ClientEntity{
        
        private String clientScrect;
        private Set<String> resourceIds = new HashSet<>(Arrays.asList("DEFAULT"));
        private Set<GrantType> grantTypes = new HashSet<>(Arrays.asList(
//                GrantType.AUTHORIZATION_CODE,
//                GrantType.CLIENT_CREDENTIALS,
//                GrantType.IMPLICIT,
                GrantType.PASSWORD,
                GrantType.REFRESH_TOKEN
                ));
        private Set<String> scopes = new HashSet<>(Arrays.asList("DEFAULT"));
        
        @Override
        public void eraseCredentials() {
            
        }

        @Override
        public String getClientId() {
            return "default";
        }

        @Override
        public Set<String> getResourceIds() {
            return resourceIds;
        }

        @Override
        public Boolean getSecretRequired() {
            return false;
        }

        @Override
        public String getClientSecret() {
            return this.clientScrect;
        }
        

        public void setClientSecret(String clientScrect) {
            this.clientScrect = clientScrect;
        }


        @Override
        public Boolean getIsScoped() {
            return false;
        }

        @Override
        public Set<String> getScope() {
            return scopes;
        }

        @Override
        public Set<GrantType> getAuthorizedGrantTypes() {
            return grantTypes;
        }

        @Override
        public Set<String> getRegisteredRedirectUri() {
            return null;
        }

        @Override
        public Collection<GrantedAuthority> getAuthorities() {
            return null;
        }

        @Override
        public Integer getAccessTokenValiditySeconds() {
            return 24 * 60 * 60;
        }

        @Override
        public Integer getRefreshTokenValiditySeconds() {
            return 7 * 24 * 60 * 60;
        }

        @Override
        public Map<String, Object> getAdditionalInformation() {
            return null;
        }

        @Override
        public Boolean getNeverNeedApprove() {
            return true;
        }

        @Override
        public Set<String> getAutoApproveScope() {
            return null;
        }

        @Override
        public Boolean getEnabled() {
            return true;
        }

    }

}
