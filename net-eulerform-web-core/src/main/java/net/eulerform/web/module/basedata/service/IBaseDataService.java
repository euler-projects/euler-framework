package net.eulerform.web.module.basedata.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.core.base.entity.PageResponse;
import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.module.basedata.entity.CodeTable;
import net.eulerform.web.module.basedata.entity.Module;

public interface IBaseDataService extends IBaseService {

    public void loadBaseData();

    public List<Module> findAllModule();

    public void createCodeDict() throws IOException;
    
    public void saveCodeTable(CodeTable codeTable);
    
    public String findConfigValue(String key);

    public void setWebRootRealPath(String webRootRealPath);

    public PageResponse<CodeTable> findCodeTableByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    public PageResponse<Module> findModuleByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    public void deleteCodeTables(Serializable[] idArray);

    public void deleteCodeTable(Serializable id);

}
