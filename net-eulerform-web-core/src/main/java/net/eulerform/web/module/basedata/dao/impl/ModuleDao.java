package net.eulerform.web.module.basedata.dao.impl;

import java.util.List;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import net.eulerform.common.StringTool;
import net.eulerform.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerform.web.core.base.entity.QueryRequest;
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
    public List<Module> findModuleByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        try {
            String queryValue = null;
            queryValue = queryRequest.getQueryValue("name");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("name", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        detachedCriteria.addOrder(Order.asc("showOrder"));
        detachedCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);        
        
        return this.findPageBy(detachedCriteria, pageIndex, pageSize);
    }
    
}
