package net.eulerframework.web.module.basic.dao;

import java.util.List;

import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.basic.entity.Dictionary;

public interface IDictionaryDao extends IBaseDao<Dictionary> {

    List<Dictionary> findAllDictionaryOrderByName();

    PageResponse<Dictionary> findDictionaryByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

}
