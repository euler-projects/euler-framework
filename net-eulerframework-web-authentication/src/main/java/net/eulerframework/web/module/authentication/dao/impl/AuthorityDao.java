package net.eulerframework.web.module.authentication.dao.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.authentication.dao.IAuthorityDao;
import net.eulerframework.web.module.authentication.entity.Authority;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import net.eulerframework.common.util.PinYinTool;
import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.core.extend.hibernate5.RestrictionsX;

public class AuthorityDao extends BaseDao<Authority> implements IAuthorityDao {

    @Override
    public PageResponse<Authority> findAuthorityByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        try {
            String queryValue = null;
            queryValue = queryRequest.getQueryValue("name");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(RestrictionsX.like("name", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("authority");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("authority", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("description");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("description", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        detachedCriteria.addOrder(Order.asc("authority"));
        
        PageResponse<Authority> result = this.findPageBy(detachedCriteria, pageIndex, pageSize);
        
        return result;
    }

    @Override
    public List<Authority> findAllAuthoritiesInOrder() {
        List<Authority> result = this.findAll();
        Comparator<Authority> c = new Comparator<Authority>() {  
            @Override  
            public int compare(Authority o1, Authority o2) {  
                return PinYinTool.toPinYinString(o1.getName()).compareTo(PinYinTool.toPinYinString(o2.getName()));  
            }
        };
        Collections.sort(result, c);
        return result;
    }
    
    

}
