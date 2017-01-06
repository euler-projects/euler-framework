package net.eulerframework.web.module.basic.dao;

import java.util.List;

import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.module.basic.entity.Module;

public interface IModuleDao extends IBaseDao<Module> {

    List<Module> findAllInOrder();

}
