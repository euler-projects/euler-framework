package net.eulerframework.web.module.basedata.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.module.basedata.entity.CodeTable;
import net.eulerframework.web.module.basedata.entity.Module;
import net.eulerframework.web.module.basedata.entity.Page;

public interface IBaseDataService extends IBaseService {

    public void loadBaseData();

    public void createCodeDict() throws IOException;

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','ROOT')")
    public void saveCodeTable(CodeTable codeTable);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','ROOT')")
    public void deleteCodeTables(Serializable[] idArray);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','ROOT')")
    public List<Module> findAllModuleFromDB();

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','ROOT')")
    public PageResponse<CodeTable> findCodeTableByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','ROOT')")
    public Module findModuleById(String id);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','ROOT')")
    public Page findPageById(String id);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','ROOT')")
    public void savePage(Page page);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','ROOT')")
    public void deletePage(Serializable id);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','ROOT')")
    public void saveModule(Module module);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','ROOT')")
    public void deleteModule(Serializable id);

}
