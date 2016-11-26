package net.eulerframework.web.module.cms.basic.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.core.exception.MultipartFileSaveException;
import net.eulerframework.web.module.cms.basic.entity.News;

@PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('CMS_ADMIN','ADMIN','SYSTEM')")
public interface INewsService extends IBaseService {

    @PreAuthorize("permitAll")
    public News findNews(String newsId);
    
    @PreAuthorize("permitAll")
    public PageResponse<News> findNewsByPage(QueryRequest queryRequest, int pageIndex, int pageSize, boolean loadText, boolean enableTop);
    
    public void saveNews(News news, MultipartFile img) throws MultipartFileSaveException;

    public void deleteNews(String[] idArray);

    void deleteImg(String newsId);


}
