package net.eulerform.web.module.basedata.dao.impl;

import java.util.List;

import net.eulerform.web.core.base.dao.hibernate5.impl.BaseDao;
import net.eulerform.web.module.basedata.dao.ICodeTableDao;
import net.eulerform.web.module.basedata.entity.CodeTable;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

public class CodeTableDao extends BaseDao<CodeTable> implements ICodeTableDao {

    @Override
    public List<CodeTable> findAllCodeOrderByName() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        detachedCriteria.addOrder(Order.asc("name"));
        detachedCriteria.addOrder(Order.asc("showOrder"));
        return this.findBy(detachedCriteria);
    }
    
}
