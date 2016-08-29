package net.eulerform.web.module.blog.dao;

import net.eulerform.web.core.base.dao.IBaseDao;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.module.blog.entity.Blog;

public interface IBlogDao extends IBaseDao<Blog> {

    public PageResponse<Blog> findBlogByPage(QueryRequest queryRequest, int pageIndex, int pageSize, boolean loadText, boolean enableTop);

}
