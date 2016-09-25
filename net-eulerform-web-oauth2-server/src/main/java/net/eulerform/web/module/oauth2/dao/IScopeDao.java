package net.eulerform.web.module.oauth2.dao;

import java.util.List;

import net.eulerform.web.core.base.dao.IBaseDao;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.module.oauth2.entity.Scope;

public interface IScopeDao extends IBaseDao<Scope> {

    PageResponse<Scope> findScopeByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    List<Scope> findAllScopesInOrder();
    
}
