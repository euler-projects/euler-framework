package net.eulerform.web.module.demo.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import net.eulerform.common.FileReader;
import net.eulerform.web.core.annotation.WebController;
import net.eulerform.web.core.base.controller.BaseController;
import net.eulerform.web.module.demo.entity.Blog;
import net.eulerform.web.module.demo.service.IBlogService;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@WebController
@Scope("prototype")
@RequestMapping("/demo")
public class BlogWebController extends BaseController {
    
    @Resource
    private IBlogService blogService;

    @RequestMapping(value = "/loadBlog", method = RequestMethod.GET)
    public void test(HttpServletResponse response) throws IOException {
        String webInfPath = this.getServletContext().getRealPath("/WEB-INF/");
        String result = FileReader.readFileByLines(webInfPath+"/dev-resources/json/blogs.json");
        response.setContentType("application/json;charset=UTF-8");
        this.writeString(response, result);
    }
    
    @RequestMapping(value = "/loadBlogBody/{id}", method = RequestMethod.GET)
    public void test(HttpServletResponse response, @PathVariable("id") long id)
            throws IOException {
        String webInfPath = this.getServletContext().getRealPath("/WEB-INF/");
        String result = FileReader.readFileByLines(webInfPath+"/dev-resources/blogs/"+id+".blog");
        response.setContentType("text/plain;charset=UTF-8");
        this.writeString(response, result);
    }

    @ResponseBody
    @RequestMapping(value = "/loadBlog/all", method = RequestMethod.GET)
    public List<Blog> findAllBlogs() {
        return this.blogService.findAllBlogs();
    }
    
    public static void main(String[] args) throws JsonProcessingException{
        Blog blog = new Blog();
        blog.setId(Long.MAX_VALUE);
        Date date = new Date();
        blog.setCreateDate(date);
        
         
        ObjectMapper mapper = new ObjectMapper();
        //String json = mapper.writeValueAsString(blog);
        //System.out.println(json);
        mapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        String json2 = mapper.writeValueAsString(blog);
        System.out.println(json2);
    }
}
