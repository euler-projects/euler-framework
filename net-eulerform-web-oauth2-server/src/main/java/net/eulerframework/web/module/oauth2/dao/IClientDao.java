package net.eulerframework.web.module.oauth2.dao;

import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.oauth2.entity.Client;

public interface IClientDao extends IBaseDao<Client> {

    Client findClientByClientId(String clientId);

    PageResponse<Client> findClientByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

}
