package net.eulerframework.web.module.blog.dao;

import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.blog.entity.Blog;

public interface IBlogDao extends IBaseDao<Blog> {

    public PageResponse<Blog> findBlogByPage(QueryRequest queryRequest, int pageIndex, int pageSize, boolean loadText, boolean enableTop);

}
