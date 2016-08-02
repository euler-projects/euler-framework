package net.eulerform.web.module.demo.controller;

import java.text.ParseException;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContext;

import net.eulerform.web.core.annotation.RestEndpoint;
import net.eulerform.web.core.base.controller.BaseRest;
import net.eulerform.web.core.base.entity.WebServiceResponse;
import net.eulerform.web.module.demo.entity.Blog;
import net.eulerform.web.module.demo.service.IBlogService;

@RestEndpoint
@Scope("prototype")
@RequestMapping("/demo")
public class DemoRestEndpoint extends BaseRest {
    
    //@Resource
    private IBlogService blogService;

    @ResponseBody
    @RequestMapping(value = { "/test" }, method = RequestMethod.GET)
    public WebServiceResponse<String> createClient(String date,HttpServletRequest request) throws ParseException {
        RequestContext requestContext = new RequestContext(request);
        Locale myLocale = requestContext.getLocale();
        System.out.println(myLocale);
        String message = requestContext.getMessage("test");
        return new WebServiceResponse<>(myLocale.toString()+" " +message, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = "/loadBlog/all", method = RequestMethod.GET)
    public WebServiceResponse<Blog> findAllBlogs() {
    	WebServiceResponse<Blog> wsResponse = new WebServiceResponse<Blog>();
    	wsResponse.setData(this.blogService.findAllBlogs());
    	wsResponse.setStatus(HttpStatus.OK);
        return wsResponse;
    }

    @ResponseBody
    @RequestMapping(value = "/loadBlog/{id}", method = RequestMethod.GET)
    public WebServiceResponse<Blog> findBlog(@PathVariable("id") long id) {
    	WebServiceResponse<Blog> wsResponse = new WebServiceResponse<Blog>();
    	wsResponse.setData(this.blogService.find(id));
    	wsResponse.setStatus(HttpStatus.OK);
        return wsResponse;
    }

    @ResponseBody
    @RequestMapping(value = "/findBlogByName/{name}", method = RequestMethod.GET)
    public WebServiceResponse<Blog> findBlogByName(@PathVariable("name") String name) {
        WebServiceResponse<Blog> wsResponse = new WebServiceResponse<Blog>();
        wsResponse.setData(this.blogService.findBlogByName(name));
        wsResponse.setStatus(HttpStatus.OK);
        return wsResponse;
    }
    
    @RequestMapping(value={"/createBlog"},method=RequestMethod.POST)
    @ResponseBody
    public Blog newBlog() {
        return this.blogService.createBlog();
    }
}
