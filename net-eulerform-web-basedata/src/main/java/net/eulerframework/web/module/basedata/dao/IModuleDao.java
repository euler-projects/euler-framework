package net.eulerframework.web.module.basedata.dao;

import java.util.List;

import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.module.basedata.entity.Module;

public interface IModuleDao extends IBaseDao<Module> {

    List<Module> findAllInOrder();

}
