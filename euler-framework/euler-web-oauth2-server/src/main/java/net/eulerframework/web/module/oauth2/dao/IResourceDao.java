package net.eulerframework.web.module.oauth2.dao;

import java.util.List;

import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.oauth2.entity.Resource;

public interface IResourceDao extends IBaseDao<Resource> {

    PageResponse<Resource> findResourceByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    List<Resource> findAllResourcesInOrder();
    
}
