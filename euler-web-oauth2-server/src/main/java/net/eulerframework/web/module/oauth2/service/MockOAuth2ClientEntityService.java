/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2018 Euler Project 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following websites
 * 
 * https://eulerproject.io
 */
package net.eulerframework.web.module.oauth2.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
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
        private Set<String> resourceIds = new HashSet<>(Arrays.asList("default"));
        private Set<GrantType> grantTypes = new HashSet<>(Arrays.asList(
//                GrantType.AUTHORIZATION_CODE,
//                GrantType.CLIENT_CREDENTIALS,
//                GrantType.IMPLICIT,
                GrantType.PASSWORD,
                GrantType.REFRESH_TOKEN
                ));
        private Set<String> scopes = new HashSet<>(Arrays.asList("default"));
        
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
