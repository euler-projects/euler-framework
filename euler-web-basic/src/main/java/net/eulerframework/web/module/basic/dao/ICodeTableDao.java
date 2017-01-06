package net.eulerframework.web.module.basic.dao;

import java.util.List;

import net.eulerframework.common.email.EmailConfig;
import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.basic.entity.CodeTable;

public interface ICodeTableDao extends IBaseDao<CodeTable> {

    List<CodeTable> findAllCodeOrderByName();

    List<CodeTable> findAllConfig();

    CodeTable findConfig(String key);

    PageResponse<CodeTable> findCodeTableByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    EmailConfig findSysEmailConfig();

}
