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
