package net.eulerform.web.module.basedata.dao;

import java.util.List;

import net.eulerform.web.core.base.dao.IBaseModifyInfoDao;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.module.basedata.entity.CodeTable;

public interface ICodeTableDao extends IBaseModifyInfoDao<CodeTable> {

    List<CodeTable> findAllCodeOrderByName();

    List<CodeTable> findAllConfig();

    CodeTable findConfig(String key);

    List<CodeTable> findCodeTableByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

}
