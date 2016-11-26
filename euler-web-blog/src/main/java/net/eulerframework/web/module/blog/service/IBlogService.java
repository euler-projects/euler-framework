package net.eulerframework.web.module.blog.service;

import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.exception.MultipartFileSaveException;
import net.eulerframework.web.module.blog.entity.Blog;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import net.eulerframework.web.core.base.service.IBaseService;

@PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('BLOG_ADMIN','ADMIN','SYSTEM')")
public interface IBlogService extends IBaseService {

    @PreAuthorize("permitAll")
    public Blog findBlog(String blogId);
    
    @PreAuthorize("permitAll")
    public PageResponse<Blog> findBlogByPage(QueryRequest queryRequest, int pageIndex, int pageSize, boolean loadText, boolean enableTop);
    
    public void saveBlog(Blog blog, MultipartFile img) throws MultipartFileSaveException;

    public void deleteBlog(String[] idArray);

    void deleteImg(String blogId);


}
