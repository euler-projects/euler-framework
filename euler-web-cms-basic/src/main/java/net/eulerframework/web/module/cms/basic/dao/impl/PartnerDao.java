package net.eulerframework.web.module.cms.basic.dao.impl;

import java.util.List;

import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.cms.basic.dao.IPartnerDao;
import net.eulerframework.web.module.cms.basic.entity.Partner;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.core.extend.hibernate5.RestrictionsX;

public class PartnerDao extends BaseDao<Partner> implements IPartnerDao {

    @Override
    public int findMaxOrder() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);        
        detachedCriteria.addOrder(Order.desc("order"));
        List<Partner> result = this.findByWithMaxResults(detachedCriteria, 1);
        if(result == null || result.isEmpty())
            return 0;
        
        return result.get(0).getOrder();
    }

    @Override
    public PageResponse<Partner> findPartnerByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        try {
            String queryValue = null;
            queryValue = queryRequest.getQueryValue("name");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(RestrictionsX.like("name", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("summary");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(RestrictionsX.like("summary", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("show");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.eq("show", Boolean.parseBoolean(queryValue)));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        detachedCriteria.addOrder(Order.asc("order"));
        
        PageResponse<Partner> result = this.findPageBy(detachedCriteria, pageIndex, pageSize);
        
        return result;
    }

    @Override
    public List<Partner> loadPartners(boolean onlyShow) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass); 
        if(onlyShow) {
            detachedCriteria.add(Restrictions.eq("show", true));
        }
        detachedCriteria.addOrder(Order.asc("order"));
        return this.findBy(detachedCriteria);
    }

    @Override
    public List<Partner> findPartnerByNameFuzzy(String name) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        detachedCriteria.add(RestrictionsX.like("name", name, MatchMode.ANYWHERE).ignoreCase());
        return this.findBy(detachedCriteria);
    }

}
