package net.eulerform.web.module.demo.controller;

import javax.annotation.Resource;

import net.eulerform.web.core.annotation.RestEndpoint;
import net.eulerform.web.core.base.controller.BaseRest;
import net.eulerform.web.core.base.entity.WebServiceResponse;
import net.eulerform.web.core.base.entity.WebServiceResponseStatus;
import net.eulerform.web.module.demo.entity.Blog;
import net.eulerform.web.module.demo.service.IBlogService;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RestEndpoint
@Scope("prototype")
@RequestMapping("/demo")
public class BlogRestEndpoint extends BaseRest {
    
    @Resource
    private IBlogService blogService;

    @ResponseBody
    @RequestMapping(value = "/loadBlog/all", method = RequestMethod.GET)
    public WebServiceResponse<Blog> findAllBlogs() {
    	WebServiceResponse<Blog> wsResponse = new WebServiceResponse<Blog>();
    	wsResponse.setData(this.blogService.findAllBlogs());
    	wsResponse.setStatus(WebServiceResponseStatus.OK);
        return wsResponse;
    }

    @ResponseBody
    @RequestMapping(value = "/loadBlog/{id}", method = RequestMethod.GET)
    public WebServiceResponse<Blog> findBlog(@PathVariable("id") long id) {
    	WebServiceResponse<Blog> wsResponse = new WebServiceResponse<Blog>();
    	wsResponse.setData(this.blogService.find(id));
    	wsResponse.setStatus(WebServiceResponseStatus.OK);
        return wsResponse;
    }

    @ResponseBody
    @RequestMapping(value = "/findBlogByName/{name}", method = RequestMethod.GET)
    public WebServiceResponse<Blog> findBlogByName(@PathVariable("name") String name) {
        WebServiceResponse<Blog> wsResponse = new WebServiceResponse<Blog>();
        wsResponse.setData(this.blogService.findBlogByName(name));
        wsResponse.setStatus(WebServiceResponseStatus.OK);
        return wsResponse;
    }
    
    @RequestMapping(value={"/createBlog"},method=RequestMethod.POST)
    @ResponseBody
    public Blog newBlog() {
        return this.blogService.createBlog();
    }
}
