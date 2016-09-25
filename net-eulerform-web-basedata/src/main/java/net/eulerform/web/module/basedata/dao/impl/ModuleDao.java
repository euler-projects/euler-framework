package net.eulerform.web.module.basedata.dao.impl;

import java.util.List;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

import net.eulerform.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerform.web.module.basedata.dao.IModuleDao;
import net.eulerform.web.module.basedata.entity.Module;

public class ModuleDao extends BaseDao<Module> implements IModuleDao {

    @Override
    public List<Module> findAllInOrder() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        detachedCriteria.addOrder(Order.asc("showOrder"));
        detachedCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        return this.findBy(detachedCriteria);
    }

    @Override
    public void flushSession() {        
        this.getCurrentSession().flush();
    }
    
}
