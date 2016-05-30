package net.eulerform.web.module.basedata.dao;

import java.util.List;

import net.eulerform.web.core.base.dao.hibernate5.IBaseDao;
import net.eulerform.web.module.basedata.entity.CodeTable;

public interface ICodeTableDao extends IBaseDao<CodeTable> {

    List<CodeTable> findAllCodeOrderByName();

}
