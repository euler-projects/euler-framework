package net.eulerform.web.module.cms.basic.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import net.eulerform.web.core.base.entity.PageResponse;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.core.exception.MultipartFileSaveException;
import net.eulerform.web.module.cms.basic.entity.News;

@PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('CMS_ADMIN','ADMIN','SYSTEM')")
public interface INewsService extends IBaseService {

    @PreAuthorize("permitAll")
    public PageResponse<News> findNewsByPage(QueryRequest queryRequest, int pageIndex, int pageSize, boolean loadText, boolean enableTop);
    
    public void saveNews(News news, MultipartFile img) throws MultipartFileSaveException;

    public void deleteNews(String[] idArray);

}
