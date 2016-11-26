package net.eulerframework.web.module.blog.controller;

import java.text.ParseException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import net.eulerframework.web.core.base.controller.DefaultWebController;
import net.eulerframework.web.core.base.response.PageResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import net.eulerframework.common.util.CalendarTool;
import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.exception.MultipartFileSaveException;
import net.eulerframework.web.module.blog.entity.Blog;
import net.eulerframework.web.module.blog.service.IBlogService;

@WebController
@Scope("prototype")
@RequestMapping("/manage/blog")
public class BlogManageWebController extends DefaultWebController {

    @Resource IBlogService blogService;
    
    @RequestMapping(value = "/blog", method = RequestMethod.GET)
    public String blog() {
        return "/manage/blog/blog";
    }

    @ResponseBody
    @RequestMapping(value ="/findBlogByPage")
    public PageResponse<Blog> findBlogByPage(HttpServletRequest request, String page, String rows) {
        QueryRequest queryRequest = new QueryRequest(request);
        
        int pageIndex = Integer.parseInt(page);
        int pageSize = Integer.parseInt(rows);
        return this.blogService.findBlogByPage(queryRequest, pageIndex, pageSize, true, true);
    }

    @ResponseBody
    @RequestMapping(value = "/saveBlog", method = RequestMethod.POST)
    public void saveBlog(
            @RequestParam(value = "img", required = false) MultipartFile img,
            @RequestParam(value = "pubDateStr", required = true) String pubDateStr,
            @Valid Blog blog) throws MultipartFileSaveException, ParseException {
        
        blog.setPubDate(CalendarTool.parseDate(pubDateStr, "yyyy-MM-dd HH:mm:ss"));
        this.blogService.saveBlog(blog, img);
    }
    
    @ResponseBody
    @RequestMapping(value = "/deleteBlog", method = RequestMethod.POST)
    public void deleteBlog(@RequestParam String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(";");
        this.blogService.deleteBlog(idArray);
    }
}
