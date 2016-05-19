package net.eulerform.web.core.security.authentication.service.impl;

import java.util.HashSet;
import java.util.Set;

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
        Client c = new Client();
        c.setId(clientId);
        c.setClientId(clientId);
        Set<String> authorizedGrantTypes = new HashSet<>();
        authorizedGrantTypes.add("authorization_code");
        Set<String> scope = new HashSet<>();
        scope.add("READ");
        scope.add("WRITE");
        scope.add("TRUST");
        c.setAuthorizedGrantTypes(authorizedGrantTypes);
        c.setScope(scope);
        Set<String> ruri = new HashSet<>();
        ruri.add("http://10.88.5.166:7070/efb/webapi/svn/workingcopy");
        ruri.add("http://10.88.5.166:7070/efb/getToken.html");
        c.setRegisteredRedirectUri(ruri);
        return c;
//        Client client = this.clientDao.findClientByClientId(clientId);
//        if(client == null)
//            throw new ClientRegistrationException("Client \"" + clientId + "\" not found");
//        return client;
    }    
    
}
