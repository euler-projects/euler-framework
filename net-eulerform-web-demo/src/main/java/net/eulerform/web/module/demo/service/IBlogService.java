package net.eulerform.web.module.demo.service;

import java.util.List;

import net.eulerform.web.module.demo.entity.Blog;

//@PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('VIEW_BLOG', 'ADMIN')")
public interface IBlogService {
    public List<Blog> findAllBlogs();
    public Blog createBlog();
    public Blog find(long id);
    public List<Blog> findBlogByName(String name);
}
