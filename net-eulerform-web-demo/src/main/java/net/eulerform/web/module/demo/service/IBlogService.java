package net.eulerform.web.module.demo.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerform.web.core.base.service.IBaseSecurityService;
import net.eulerform.web.module.demo.entity.Blog;

@PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('SYSTEM','ADMIN')")
public interface IBlogService extends IBaseSecurityService {
    public List<Blog> findAllBlogs();
    public Blog createBlog();
    public Blog find(long id);
    public List<Blog> findBlogByName(String name);
}
