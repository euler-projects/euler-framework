package net.eulerform.web.module.authentication.service;

import java.util.List;

import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.module.authentication.entity.Client;
import net.eulerform.web.module.authentication.entity.Resource;
import net.eulerform.web.module.authentication.entity.Scope;

public interface IClientService extends IBaseService {

    void createScope(Scope scope);

    void createResource(Resource resource);

    List<Client> findAllClient();
    
    public void createClient(String secret, Integer accessTokenValiditySeconds, Integer refreshTokenValiditySeconds, Boolean neverNeedApprove);
    
}
