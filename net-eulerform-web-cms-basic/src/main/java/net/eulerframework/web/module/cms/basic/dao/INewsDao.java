package net.eulerframework.web.module.cms.basic.dao;

import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.cms.basic.entity.News;

public interface INewsDao extends IBaseDao<News> {

    public PageResponse<News> findNewsByPage(QueryRequest queryRequest, int pageIndex, int pageSize, boolean loadText, boolean enableTop);

}
