package net.eulerform.web.module.basedata.service;

import java.io.IOException;

import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.module.basedata.entity.CodeTable;

public interface ICodeTableService extends IBaseService {

    public void createCodeDict() throws IOException;
    
    public void saveCodeTable(CodeTable codeTable);
}
