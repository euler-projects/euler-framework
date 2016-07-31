package net.eulerform.web.module.authentication.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

import net.eulerform.common.BeanTool;
import net.eulerform.common.StringTool;
import net.eulerform.web.core.base.entity.PageResponse;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.core.cache.ObjectCache;
import net.eulerform.web.module.authentication.dao.IClientDao;
import net.eulerform.web.module.authentication.dao.IResourceDao;
import net.eulerform.web.module.authentication.dao.IScopeDao;
import net.eulerform.web.module.authentication.entity.Client;
import net.eulerform.web.module.authentication.entity.GrantType;
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
        
        if(!client.isEnabled())
            throw new ClientRegistrationException("Client \"" + clientId + "\" is disabled");
        
        if(cacheEnabled) {
            this.clientCache.put(clientId, client);
        }
        
        return client;
    }

    @Override
    public PageResponse<Client> findClientByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        return this.clientDao.findClientByPage(queryRequest, pageIndex, pageSize);
    }

    @Override
    public PageResponse<Resource> findResourceByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        return this.resourceDao.findResourceByPage(queryRequest, pageIndex, pageSize);
    }

    @Override
    public PageResponse<Scope> findScopeByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        return this.scopeDao.findScopeByPage(queryRequest, pageIndex, pageSize);
    }

    @Override
    public void saveClient(Client client) {
        BeanTool.clearEmptyProperty(client);
        if(client.getId() != null) {
            Client tmp = null;
            if(client.getClientSecret() == null) {
                if(tmp == null) {
                    tmp = this.clientDao.load(client.getId());
                }
                client.setClientSecret(tmp.getClientSecret());               
            }

            if(client.getResources() == null) {
                if(tmp == null) {
                    tmp = this.clientDao.load(client.getId());
                }
                client.setResources(tmp.getResources());               
            }
            if(client.getScopes() == null) {
                if(tmp == null) {
                    tmp = this.clientDao.load(client.getId());
                }
                client.setScopes(tmp.getScopes());               
            }
            if(client.getRegisteredRedirectUri() == null) {
                if(tmp == null) {
                    tmp = this.clientDao.load(client.getId());
                }
                client.setRegisteredRedirectUri(tmp.getRegisteredRedirectUri());               
            }
            if(client.getAuthorizedGrantTypes() == null) {
                if(tmp == null) {
                    tmp = this.clientDao.load(client.getId());
                }

                Set<String> grantType = tmp.getAuthorizedGrantTypes();
                if(grantType != null && !grantType.isEmpty()) {
                    Set<GrantType> grantTypes = new HashSet<>();
                    for(String grantTypeStr : grantType) {
                        GrantType result = GrantType.getGrantType(grantTypeStr);
                        if(result != null) {
                            grantTypes.add(result);
                        }
                    }
                    client.setAuthorizedGrantTypes(grantTypes);
                }
            }
        }
        
        if(client.getClientSecret() == null) {
            client.setClientSecret(this.passwordEncoder.encode("sf123456"));
        }
        
        this.clientDao.saveOrUpdate(client);
    }

    @Override
    public void saveClient(Client client, String[] grantType, String scopesIds, String resourceIds,
            String redirectUris) {
        if(grantType != null && grantType.length > 0) {
            Set<GrantType> grantTypes = new HashSet<>();
            for(String grantTypeStr : grantType) {
                GrantType result = GrantType.getGrantType(grantTypeStr);
                if(result != null) {
                    grantTypes.add(result);
                }
            }
            client.setAuthorizedGrantTypes(grantTypes);
        }
        
        if(!StringTool.isNull(redirectUris)) {
            client.setRegisteredRedirectUri(new HashSet<>(Arrays.asList(redirectUris.trim().split(";"))));
        }
        
        if(!StringTool.isNull(scopesIds)) {
            List<net.eulerform.web.module.authentication.entity.Scope> scopes 
             = this.findScopesByIdArray(scopesIds.trim().split(";"));
            if(scopes != null && !scopes.isEmpty()) {
                client.setScopes(new HashSet<>(scopes));
            }
        }
        
        if(!StringTool.isNull(resourceIds)) {
            List<net.eulerform.web.module.authentication.entity.Resource> resources 
             = this.findResourceByIdArray(resourceIds.trim().split(";"));
            if(resources != null && !resources.isEmpty()) {
                client.setResources(new HashSet<>(resources));
            }
        }

        BeanTool.clearEmptyProperty(client);
        if(client.getId() != null) {
            Client tmp = null;
            if(client.getClientSecret() == null) {
                if(tmp == null) {
                    tmp = this.clientDao.load(client.getId());
                }
                client.setClientSecret(tmp.getClientSecret());               
            }
        }
        
        if(client.getClientSecret() == null) {
            client.setClientSecret(this.passwordEncoder.encode("sf123456"));
        }
        
        this.clientDao.saveOrUpdate(client);
        
    }

    @Override
    public void saveResource(Resource resource) {
        this.resourceDao.saveOrUpdate(resource);
    }

    @Override
    public void saveScope(Scope scope) {
        this.scopeDao.saveOrUpdate(scope);
    }

    @Override
    public void enableClientsRWT(String[] idArray) {
        List<Client> clients = this.clientDao.load(idArray);
        
        for(Client client : clients) {
            client.setEnabled(true);
        }
        
        this.clientDao.saveOrUpdate(clients);
    }

    @Override
    public void disableClientsRWT(String[] idArray) {
        List<Client> clients = this.clientDao.load(idArray);
        
        for(Client client : clients) {
            client.setEnabled(false);
        }
        
        this.clientDao.saveOrUpdate(clients);
    }

    @Override
    public void deleteResources(String[] idArray) {
        this.resourceDao.deleteByIds(idArray);
    }

    @Override
    public void deleteScopes(String[] idArray) {
        this.scopeDao.deleteByIds(idArray);
    }

    @Override
    public Client findClientByClientId(String clientId) {

        Client client = this.clientDao.findClientByClientId(clientId);
        if (client == null)
            throw new ClientRegistrationException("Client \"" + clientId + "\" not found");
        
        if(cacheEnabled) {
            this.clientCache.put(clientId, client);
        }
        
        return client;
    }

    @Override
    public List<Scope> findScopesByIdArray(String[] idArray) {
        return this.scopeDao.load(idArray);
    }

    @Override
    public List<Resource> findResourceByIdArray(String[] idArray) {
        return this.resourceDao.load(idArray);
    }

}
