package net.eulerform.web.module.basedata.dao;

import java.util.List;

import net.eulerform.common.email.EmailConfig;
import net.eulerform.web.core.base.dao.IBaseDao;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.module.basedata.entity.CodeTable;

public interface ICodeTableDao extends IBaseDao<CodeTable> {

    List<CodeTable> findAllCodeOrderByName();

    List<CodeTable> findAllConfig();

    CodeTable findConfig(String key);

    PageResponse<CodeTable> findCodeTableByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    EmailConfig findSysEmailConfig();

}
