package net.eulerframework.web.module.blog.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.core.annotation.RestEndpoint;
import net.eulerframework.web.core.base.controller.AbstractRestEndpoint;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.WebServicePageResponse;
import net.eulerframework.web.core.base.response.WebServiceResponse;
import net.eulerframework.web.module.blog.entity.Blog;
import net.eulerframework.web.module.blog.service.IBlogService;

@RestEndpoint
@Scope("prototype")
@RequestMapping("/blog")
public class BlogRestEndpoint extends AbstractRestEndpoint {

    @Resource IBlogService blogService;

    @ResponseBody
    @RequestMapping(value ="/findBlogByPage", method = RequestMethod.GET)
    public WebServicePageResponse<Blog> findBlogByPage(HttpServletRequest request, int pageIndex, int pageSize, boolean loadText, boolean enableTop) {
        QueryRequest queryRequest = new QueryRequest(request);
        return new WebServicePageResponse<>(this.blogService.findBlogByPage(queryRequest, pageIndex, pageSize, loadText, enableTop));
    }

    @ResponseBody
    @RequestMapping(value ="/{newId}", method = RequestMethod.GET)
    public WebServiceResponse<Blog> findBlog(@PathVariable("newId") String blogId) {
        return new WebServiceResponse<>(this.blogService.findBlog(blogId));
    }
}
