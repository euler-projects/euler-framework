package net.eulerform.web.module.basedata.service;

import java.io.IOException;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerform.web.core.base.service.IBaseSecurityService;
import net.eulerform.web.module.basedata.entity.CodeTable;

@PreAuthorize("permitAll")
public interface ICodeTableService extends IBaseSecurityService {

    public void createCodeDict() throws IOException;
    
    public void saveCodeTable(CodeTable codeTable);
}
