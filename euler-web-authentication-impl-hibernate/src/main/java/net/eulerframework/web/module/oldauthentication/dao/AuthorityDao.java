package net.eulerframework.web.module.oldauthentication.dao;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import net.eulerframework.common.util.StringUtils;
import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.extend.hibernate5.RestrictionsX;
import net.eulerframework.web.module.oldauthentication.entity.Authority;

@Repository
public class AuthorityDao extends BaseDao<Authority> {
    
    @Resource public void setSessionFactory(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }

    
    public PageResponse<Authority> findAuthorityByPage(PageQueryRequest pageQueryRequest) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        try {
            String queryValue = null;
            queryValue = pageQueryRequest.getQueryValue("name");
            if (!StringUtils.isNull(queryValue)) {
                detachedCriteria.add(RestrictionsX.like("name", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = pageQueryRequest.getQueryValue("authority");
            if (!StringUtils.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("authority", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = pageQueryRequest.getQueryValue("description");
            if (!StringUtils.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("description", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        detachedCriteria.addOrder(Order.asc("authority"));
        
        PageResponse<Authority> result = this.pageQuery(detachedCriteria, pageQueryRequest.getPageIndex(), pageQueryRequest.getPageSize());
        
        return result;
    }

    
    public List<Authority> findAllAuthoritiesInOrder() {
        List<Authority> result = this.queryAll();
        Comparator<Authority> c = new Comparator<Authority>() {  
              
            public int compare(Authority o1, Authority o2) {  
                return StringUtils.toPinYinString(o1.getName()).compareTo(StringUtils.toPinYinString(o2.getName()));  
            }
        };
        Collections.sort(result, c);
        return result;
    }
    
    

}
