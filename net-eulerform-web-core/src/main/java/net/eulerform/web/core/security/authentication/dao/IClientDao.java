package net.eulerform.web.core.security.authentication.dao;

import net.eulerform.web.core.base.dao.hibernate5.IBaseDao;
import net.eulerform.web.core.security.authentication.entity.Client;

public interface IClientDao extends IBaseDao<Client> {

    Client findClientByClientId(String clientId);
}
