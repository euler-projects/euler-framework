package net.eulerform.web.module.basedata.dao;

import java.util.List;

import net.eulerform.web.core.base.dao.IBaseDao;
import net.eulerform.web.module.basedata.entity.Module;

public interface IModuleDao extends IBaseDao<Module> {

    List<Module> findAllInOrder();

}
