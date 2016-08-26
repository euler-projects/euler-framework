package net.eulerform.web.module.cms.basic.service.impl;

import java.io.File;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.eulerform.common.BeanTool;
import net.eulerform.common.FileReader;
import net.eulerform.common.StringTool;
import net.eulerform.web.core.base.entity.PageResponse;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.core.exception.MultipartFileSaveException;
import net.eulerform.web.core.util.WebConfig;
import net.eulerform.web.core.util.WebFileTool;
import net.eulerform.web.module.cms.basic.dao.INewsDao;
import net.eulerform.web.module.cms.basic.entity.News;
import net.eulerform.web.module.cms.basic.service.INewsService;

@Service
public class NewsService extends BaseService implements INewsService {

    @Resource INewsDao newsDao;
    
    @Override
    public void saveNews(News news, MultipartFile img) throws MultipartFileSaveException {
        BeanTool.clearEmptyProperty(news);        

        if(img != null && img.getSize() > 0){
            File savedFile = WebFileTool.saveMultipartFile(img);
            news.setImageFileName(savedFile.getName());
        }
        
        if(!StringTool.isNull(news.getId())){
            News tmp = this.newsDao.load(news.getId());
            if(tmp != null) {
                if(!StringTool.isNull(news.getImageFileName())){
                    //删除旧图片
                    String uploadPath = this.getServletContext().getRealPath(WebConfig.getUploadPath());
                    String filePath = uploadPath+"/"+tmp.getImageFileName();
                    FileReader.deleteFile(new File(filePath));
                } else {
                    news.setImageFileName(tmp.getImageFileName());
                }
            }
        }
        
        if(news.getPubDate() == null)
            news.setPubDate(new Date());

        if(news.getTop() == null)
            news.setTop(false);
        
        this.newsDao.saveOrUpdate(news);
    }

    @Override
    public PageResponse<News> findNewsByPage(QueryRequest queryRequest, int pageIndex, int pageSize, boolean loadText, boolean enableTop) {
        return this.newsDao.findNewsByPage(queryRequest, pageIndex, pageSize, loadText, enableTop);
    }

    @Override
    public void deleteNews(String[] idArray) {
        this.newsDao.deleteByIds(idArray);
    }

}
