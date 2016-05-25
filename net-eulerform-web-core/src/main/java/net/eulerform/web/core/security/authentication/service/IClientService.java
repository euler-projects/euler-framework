package net.eulerform.web.core.security.authentication.service;

import java.util.List;

import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.core.security.authentication.entity.Client;
import net.eulerform.web.core.security.authentication.entity.GrantType;
import net.eulerform.web.core.security.authentication.entity.Resource;
import net.eulerform.web.core.security.authentication.entity.Scope;

public interface IClientService extends IBaseService {

    void createScope(Scope scope);

    void createResource(Resource resource);

    List<Client> findAllClient();
    
    public void createClient(String secret);

	void createGrantType(GrantType grantType);
    
}
