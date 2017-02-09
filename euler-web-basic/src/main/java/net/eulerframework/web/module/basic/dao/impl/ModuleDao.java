package net.eulerframework.web.module.basic.dao.impl;

import java.util.List;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.module.basic.dao.IModuleDao;
import net.eulerframework.web.module.basic.entity.Module;

public class ModuleDao extends BaseDao<Module> implements IModuleDao {

    @Override
    public List<Module> findAllInOrder() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        detachedCriteria.addOrder(Order.asc("showOrder"));
        detachedCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        return this.query(detachedCriteria);
    }
    
}
