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
package org.eulerframework.web.module.oauth2.service;

import jakarta.annotation.Resource;

import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;

import org.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import org.eulerframework.cache.inMemoryCache.ObjectCachePool;
import org.eulerframework.web.module.oauth2.conf.OAuth2ServerConfig;
import org.eulerframework.web.module.oauth2.entity.EulerOAuth2ClientEntity;
import org.eulerframework.web.module.oauth2.provider.EulerOAuth2ClientDetails;

@Service("clientDetailsService")
public class EulerClientDetailsService implements ClientDetailsService {
    
    private final static DefaultObjectCache<String, EulerOAuth2ClientDetails> CLIENT_CAHCE 
        = ObjectCachePool.generateDefaultObjectCache(OAuth2ServerConfig.getClientDetailsCacheLife());

    private boolean clientDetailsCacheEnabled = OAuth2ServerConfig.isEnableClientDetailsCache();
    
    @Resource
    private EulerOAuth2ClientEntityService eulerOAuth2ClientEntityService;

    @Override
    public EulerOAuth2ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        EulerOAuth2ClientDetails clientDetails;
        
        if(this.clientDetailsCacheEnabled) {
            clientDetails = CLIENT_CAHCE.get(clientId, key -> this.loadClientDetails(key));
        } else {
            clientDetails = this.loadClientDetails(clientId);
        }
        
        if (clientDetails == null)
            throw new ClientRegistrationException("Client \"" + clientId + "\" not found");
        
        if(!clientDetails.isEnabled())
            throw new ClientRegistrationException("Client \"" + clientId + "\" is disabled");

        return clientDetails;
    }

    private EulerOAuth2ClientDetails loadClientDetails(String clientId) {
        EulerOAuth2ClientEntity clientEntity = this.eulerOAuth2ClientEntityService.loadClientById(clientId);
        
        if(clientEntity != null) {
            return clientEntity.toEulerOAuth2ClientDetails();
        } else {
            return null;
        }
    }
}
