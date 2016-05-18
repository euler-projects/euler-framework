package net.eulerform.web.core.security.authentication.service.impl;

import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.core.security.authentication.dao.IClientDao;
import net.eulerform.web.core.security.authentication.entity.Client;
import net.eulerform.web.core.security.authentication.service.IClientService;

import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

public class ClientService extends BaseService implements IClientService, ClientDetailsService {
    
    private IClientDao clientDao;

    public void setClientDao(IClientDao clientDao) {
        this.clientDao = clientDao;
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        Client client = this.clientDao.findClientByClientId(clientId);
        if(client == null)
            throw new ClientRegistrationException("Client \"" + clientId + "\" not found");
        return client;
    }    
    
}
