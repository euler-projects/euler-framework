package net.eulerform.web.module.basedata.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerform.common.email.EmailConfig;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.module.basedata.entity.CodeTable;
import net.eulerform.web.module.basedata.entity.Module;
import net.eulerform.web.module.basedata.entity.Page;

public interface IBaseDataService extends IBaseService {

    public void setWebRootRealPath(String webRootRealPath);

    public void loadBaseData();

    public void createCodeDict() throws IOException;
    
    public String findConfigValue(String key);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public List<Module> findAllModuleFromDB();

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public PageResponse<CodeTable> findCodeTableByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public void saveCodeTable(CodeTable codeTable);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public void deleteCodeTables(Serializable[] idArray);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public Module findModuleById(String id);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public Page findPageById(String id);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public void savePage(Page page);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public void deletePage(Serializable id);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public void saveModule(Module module);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public void deleteModule(Serializable id);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('SYS_EMAIL_ADMIN','ADMIN','SYSTEM')")
    public void saveSystemEmail(EmailConfig emailConfig);

    public EmailConfig findEmailConfig();

}
