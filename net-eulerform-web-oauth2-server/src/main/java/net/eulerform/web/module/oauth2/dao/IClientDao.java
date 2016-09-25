package net.eulerform.web.module.oauth2.dao;

import net.eulerform.web.core.base.dao.IBaseDao;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.module.oauth2.entity.Client;

public interface IClientDao extends IBaseDao<Client> {

    Client findClientByClientId(String clientId);

    PageResponse<Client> findClientByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

}
