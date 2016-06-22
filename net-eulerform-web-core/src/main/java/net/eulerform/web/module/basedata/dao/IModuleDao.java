package net.eulerform.web.module.basedata.dao;

import java.util.List;

import net.eulerform.web.core.base.dao.IBaseDao;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.module.basedata.entity.Module;

public interface IModuleDao extends IBaseDao<Module> {

    List<Module> findAllInOrder();

    List<Module> findModuleByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

}
