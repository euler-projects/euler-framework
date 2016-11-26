package net.eulerframework.web.module.cms.basic.service.impl;

import java.io.File;
import java.util.Date;

import javax.annotation.Resource;

import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.exception.MultipartFileSaveException;
import net.eulerframework.web.core.util.WebConfig;
import net.eulerframework.web.core.util.WebFileTool;
import net.eulerframework.web.module.cms.basic.dao.INewsDao;
import net.eulerframework.web.module.cms.basic.service.INewsService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.eulerframework.common.util.BeanTool;
import net.eulerframework.common.util.FileReader;
import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.cms.basic.entity.News;

@Service
public class NewsService extends BaseService implements INewsService {

    @Resource
    INewsDao newsDao;
    
    @Override
    public void saveNews(News news, MultipartFile img) throws MultipartFileSaveException {
        BeanTool.clearEmptyProperty(news);        

        if(img != null && img.getSize() > 0){
            File savedFile = WebFileTool.saveMultipartFile(img);
            news.setImageFileName(savedFile.getName());
        }
        
        if(!StringTool.isNull(news.getId())){
            if(StringTool.isNull(news.getImageFileName())){
                    News oldNews = this.newsDao.load(news.getId());
                    if(oldNews != null)
                        news.setImageFileName(oldNews.getImageFileName());
            } else {
                this.deleteImg(news.getId());
            }
        }
        
        if(news.getPubDate() == null)
            news.setPubDate(new Date());

        if(news.getTop() == null)
            news.setTop(false);
        
        this.newsDao.saveOrUpdate(news);
    }

    @Override
    public News findNews(String newsId) {
        return this.newsDao.load(newsId);
    }

    @Override
    public PageResponse<News> findNewsByPage(QueryRequest queryRequest, int pageIndex, int pageSize, boolean loadText, boolean enableTop) {
        return this.newsDao.findNewsByPage(queryRequest, pageIndex, pageSize, loadText, enableTop);
    }

    @Override
    public void deleteNews(String[] idArray) {
        this.newsDao.deleteByIds(idArray);
    }

    @Override
    public void deleteImg(String newsId) {
        News oldNews = this.newsDao.load(newsId);
        if(oldNews != null) {
            String uploadPath = this.getServletContext().getRealPath(WebConfig.getUploadPath());
            String filePath = uploadPath+"/"+oldNews.getImageFileName();
            FileReader.deleteFile(new File(filePath));
        }
    }

}
