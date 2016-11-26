package net.eulerframework.web.module.blog.service.impl;

import java.io.File;
import java.util.Date;

import javax.annotation.Resource;

import net.eulerframework.web.module.blog.dao.IBlogDao;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.eulerframework.common.util.BeanTool;
import net.eulerframework.common.util.FileReader;
import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.core.exception.MultipartFileSaveException;
import net.eulerframework.web.core.util.WebConfig;
import net.eulerframework.web.core.util.WebFileTool;
import net.eulerframework.web.module.blog.entity.Blog;
import net.eulerframework.web.module.blog.service.IBlogService;

@Service
public class BlogService extends BaseService implements IBlogService {

    @Resource
    IBlogDao blogDao;
    
    @Override
    public void saveBlog(Blog blog, MultipartFile img) throws MultipartFileSaveException {
        BeanTool.clearEmptyProperty(blog);        

        if(img != null && img.getSize() > 0){
            File savedFile = WebFileTool.saveMultipartFile(img);
            blog.setImageFileName(savedFile.getName());
        }
        
        if(!StringTool.isNull(blog.getId())){
            if(StringTool.isNull(blog.getImageFileName())){
                    Blog oldBlog = this.blogDao.load(blog.getId());
                    if(oldBlog != null)
                        blog.setImageFileName(oldBlog.getImageFileName());
            } else {
                this.deleteImg(blog.getId());
            }
        }
        
        if(blog.getPubDate() == null)
            blog.setPubDate(new Date());

        if(blog.getTop() == null)
            blog.setTop(false);
        
        this.blogDao.saveOrUpdate(blog);
    }

    @Override
    public Blog findBlog(String blogId) {
        return this.blogDao.load(blogId);
    }

    @Override
    public PageResponse<Blog> findBlogByPage(QueryRequest queryRequest, int pageIndex, int pageSize, boolean loadText, boolean enableTop) {
        return this.blogDao.findBlogByPage(queryRequest, pageIndex, pageSize, loadText, enableTop);
    }

    @Override
    public void deleteBlog(String[] idArray) {
        this.blogDao.deleteByIds(idArray);
    }

    @Override
    public void deleteImg(String blogId) {
        Blog oldBlog = this.blogDao.load(blogId);
        if(oldBlog != null) {
            String uploadPath = this.getServletContext().getRealPath(WebConfig.getUploadPath());
            String filePath = uploadPath+"/"+oldBlog.getImageFileName();
            FileReader.deleteFile(new File(filePath));
        }
    }

}
