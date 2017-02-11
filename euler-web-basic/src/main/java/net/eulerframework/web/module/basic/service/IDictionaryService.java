package net.eulerframework.web.module.basic.service;

import java.io.IOException;
import java.io.Serializable;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.module.basic.entity.Dictionary;

public interface IDictionaryService extends IBaseService {

    public void loadBaseData();

    public void createCodeDict() throws IOException;

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','ROOT')")
    public void saveCodeTable(Dictionary dictionary);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','ROOT')")
    public void deleteCodeTables(Serializable[] idArray);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','ROOT')")
    public PageResponse<Dictionary> findCodeTableByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

}
