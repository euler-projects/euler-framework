package net.eulerform.web.module.oauth2.dao.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import net.eulerform.common.PinYinTool;
import net.eulerform.common.StringTool;
import net.eulerform.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.module.oauth2.dao.IScopeDao;
import net.eulerform.web.module.oauth2.entity.Scope;

public class ScopeDao extends BaseDao<Scope> implements IScopeDao {

    @Override
    public PageResponse<Scope> findScopeByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        try {
            String queryValue = null;
            queryValue = queryRequest.getQueryValue("scope");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("scope", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("name");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("name", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("description");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("description", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("scopeNameOrCode");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.or(
                        Restrictions.like("scope", queryValue, MatchMode.ANYWHERE).ignoreCase(),
                        Restrictions.like("name", queryValue, MatchMode.ANYWHERE).ignoreCase()
                        ));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        detachedCriteria.addOrder(Order.asc("scope"));
        
        return this.findPageBy(detachedCriteria, pageIndex, pageSize);
    }

    @Override
    public List<Scope> findAllScopesInOrder() {
        List<Scope> result = this.findAll();
        Comparator<Scope> c = new Comparator<Scope>(){

            @Override
            public int compare(Scope o1, Scope o2) {
                return PinYinTool.toPinYinString(o1.getName()).compareTo(PinYinTool.toPinYinString(o2.getName()));
            }
            
        };        

        Collections.sort(result, c);
        return result;
    }

}
