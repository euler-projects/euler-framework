package net.eulerframework.web.module.basic.dao.impl;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import net.eulerframework.common.util.StringUtil;
import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.basic.dao.IDictionaryDao;
import net.eulerframework.web.module.basic.entity.Dictionary;

public class DictionaryDao extends BaseDao<Dictionary> implements IDictionaryDao {

    @Override
    public List<Dictionary> findAllDictionaryOrderByName() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        detachedCriteria.addOrder(Order.asc("name"));
        detachedCriteria.addOrder(Order.asc("showOrder"));
        return this.query(detachedCriteria);
    }

    @Override
    public PageResponse<Dictionary> findDictionaryByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        try {
            String queryValue = null;
            queryValue = queryRequest.getQueryValue("name");
            if (!StringUtil.isEmpty(queryValue)) {
                detachedCriteria.add(Restrictions.like("name", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("codeType");
            if (!StringUtil.isEmpty(queryValue)) {
                detachedCriteria.add(Restrictions.eq("codeType", Integer.parseInt(queryValue)));
            }
            queryValue = queryRequest.getQueryValue("description");
            if (!StringUtil.isEmpty(queryValue)) {
                detachedCriteria.add(Restrictions.like("description", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("value");
            if (!StringUtil.isEmpty(queryValue)) {
                detachedCriteria.add(Restrictions.like("value", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("valueZhCn");
            if (!StringUtil.isEmpty(queryValue)) {
                detachedCriteria.add(Restrictions.like("valueZhCn", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("valueEnUs");
            if (!StringUtil.isEmpty(queryValue)) {
                detachedCriteria.add(Restrictions.like("valueEnUs", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        detachedCriteria.addOrder(Order.asc("name"));
        detachedCriteria.addOrder(Order.asc("showOrder"));
        
        
        return this.pageQuery(detachedCriteria, pageIndex, pageSize);
    }
    
}
