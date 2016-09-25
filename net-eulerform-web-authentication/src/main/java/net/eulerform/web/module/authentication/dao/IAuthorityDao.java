package net.eulerform.web.module.authentication.dao;

import java.util.List;

import net.eulerform.web.core.base.dao.IBaseDao;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.module.authentication.entity.Authority;

public interface IAuthorityDao extends IBaseDao<Authority> {

    PageResponse<Authority> findAuthorityByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    List<Authority> findAllAuthoritiesInOrder();
    
}
