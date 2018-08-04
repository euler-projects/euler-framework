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
 * For more information, please visit the following website
 * 
 * https://eulerproject.io
 */
package net.eulerframework.web.module.oauth2.service;

import javax.annotation.Resource;

import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;

import net.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import net.eulerframework.cache.inMemoryCache.ObjectCachePool;
import net.eulerframework.web.module.oauth2.conf.OAuth2ServerConfig;
import net.eulerframework.web.module.oauth2.entity.EulerOAuth2ClientEntity;
import net.eulerframework.web.module.oauth2.provider.EulerOAuth2ClientDetails;

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
