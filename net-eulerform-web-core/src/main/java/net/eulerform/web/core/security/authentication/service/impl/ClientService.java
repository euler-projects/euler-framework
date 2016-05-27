package net.eulerform.web.core.security.authentication.service.impl;

import java.util.List;

import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.core.security.authentication.dao.IClientDao;
import net.eulerform.web.core.security.authentication.dao.IGrantTypeDao;
import net.eulerform.web.core.security.authentication.dao.IResourceDao;
import net.eulerform.web.core.security.authentication.dao.IScopeDao;
import net.eulerform.web.core.security.authentication.entity.Client;
import net.eulerform.web.core.security.authentication.entity.GrantType;
import net.eulerform.web.core.security.authentication.entity.Resource;
import net.eulerform.web.core.security.authentication.entity.Scope;
import net.eulerform.web.core.security.authentication.service.IClientService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

public class ClientService extends BaseService implements IClientService, ClientDetailsService {

    private IClientDao clientDao;
    private IResourceDao resourceDao;
    private IScopeDao scopeDao;
    private IGrantTypeDao grantTypeDao;

    private PasswordEncoder passwordEncoder;

    public void setClientDao(IClientDao clientDao) {
        this.clientDao = clientDao;
    }

    public void setResourceDao(IResourceDao resourceDao) {
        this.resourceDao = resourceDao;
    }

    public void setScopeDao(IScopeDao scopeDao) {
        this.scopeDao = scopeDao;
    }

    public void setGrantTypeDao(IGrantTypeDao grantTypeDao) {
        this.grantTypeDao = grantTypeDao;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        Client client = this.clientDao.load(clientId);
        if (client == null)
            throw new ClientRegistrationException("Client \"" + clientId + "\" not found");
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

    @Override
    public void createGrantType(GrantType grantType) {
        this.grantTypeDao.save(grantType);
    }

}