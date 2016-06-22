package net.eulerform.web.module.authentication.service.impl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.core.cache.ObjectCache;
import net.eulerform.web.module.authentication.dao.IClientDao;
import net.eulerform.web.module.authentication.dao.IResourceDao;
import net.eulerform.web.module.authentication.dao.IScopeDao;
import net.eulerform.web.module.authentication.entity.Client;
import net.eulerform.web.module.authentication.entity.Resource;
import net.eulerform.web.module.authentication.entity.Scope;
import net.eulerform.web.module.authentication.service.IClientService;

public class ClientService extends BaseService implements IClientService, ClientDetailsService {

    private IClientDao clientDao;
    private IResourceDao resourceDao;
    private IScopeDao scopeDao;
    
    private boolean cacheEnabled = false;
    private ObjectCache<String, Client> clientCache = new ObjectCache<>(60_000L);

    private PasswordEncoder passwordEncoder;

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public void setCacheSeconds(long cacheSecond) {
        if(cacheSecond < 60){
            this.clientCache.setDataLife(cacheSecond * 1000);            
        }
    }

    public void setClientDao(IClientDao clientDao) {
        this.clientDao = clientDao;
    }

    public void setResourceDao(IResourceDao resourceDao) {
        this.resourceDao = resourceDao;
    }

    public void setScopeDao(IScopeDao scopeDao) {
        this.scopeDao = scopeDao;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {

        if(cacheEnabled) {
            Client cachedClient = this.clientCache.get(clientId);
            if(cachedClient != null) {
                return cachedClient;
            }
        }
        
        Client client = this.clientDao.findClientByClientId(clientId);
        if (client == null)
            throw new ClientRegistrationException("Client \"" + clientId + "\" not found");
        
        if(cacheEnabled) {
            this.clientCache.put(clientId, client);
        }
        
        return client;
    }

    @Override
    public void createScope(Scope scope) {
        this.scopeDao.save(scope);
    }

    @Override
    public void createResource(Resource resource) {
        this.resourceDao.save(resource);
    }

    @Override
    public List<Client> findAllClient() {
        return this.clientDao.findAll();
    }

    @Override
    public void createClient(String secret, Integer accessTokenValiditySeconds, Integer refreshTokenValiditySeconds, Boolean neverNeedApprove) {
        Client client = new Client();
        client.setClientSecret(this.passwordEncoder.encode(secret));
        client.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
        client.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
        client.setNeverNeedApprove(neverNeedApprove);
        this.clientDao.save(client);
    }

}
