package net.eulerform.web.module.demo.service.impl;

import java.util.List;

import net.eulerform.web.core.base.service.impl.BaseSecurityService;
import net.eulerform.web.module.demo.dao.IBlogDao;
import net.eulerform.web.module.demo.entity.Blog;
import net.eulerform.web.module.demo.service.IBlogService;

import org.springframework.transaction.annotation.Transactional;

public class BlogService extends BaseSecurityService implements IBlogService {

    private IBlogDao blogDao;
    
    public void setBlogDao(IBlogDao blogDao) {
        this.blogDao = blogDao;
    }
    
    @Override
    public Blog createBlog() {
        Blog blog = new Blog();
        blog.setName("新ABC");
//        List<Blog> blogs =  this.blogDao.findBy(blog);
//        this.blogDao.deleteAll(blogs);
        this.blogDao.saveOrUpdate(blog);        
        return blog;
    }

    @Override
    public List<Blog> findAllBlogs() {
        return this.blogDao.findAll();
    }

    @Override
    @Transactional
    public Blog find(long id) {
        return this.blogDao.load(id);
    }

    @Override
    public List<Blog> findBlogByName(String name) {
        Blog b = new Blog();
        b.setName(name);
        
        return this.blogDao.findBy(b);
    }
}
