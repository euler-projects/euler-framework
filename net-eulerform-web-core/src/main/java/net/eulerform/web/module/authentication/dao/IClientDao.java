package net.eulerform.web.module.authentication.dao;

import net.eulerform.web.core.base.dao.IBaseDao;
import net.eulerform.web.core.base.entity.PageResponse;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.module.authentication.entity.Client;

public interface IClientDao extends IBaseDao<Client> {

    Client findClientByClientId(String clientId);

    PageResponse<Client> findClientByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

}
