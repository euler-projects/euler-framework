package net.eulerform.web.module.oauth2.dao.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import net.eulerform.common.util.PinYinTool;
import net.eulerform.common.util.StringTool;
import net.eulerform.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.module.oauth2.dao.IResourceDao;
import net.eulerform.web.module.oauth2.entity.Resource;

public class ResourceDao extends BaseDao<Resource> implements IResourceDao {

    @Override
    public PageResponse<Resource> findResourceByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        try {
            String queryValue = null;
            queryValue = queryRequest.getQueryValue("resourceId");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("resourceId", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("name");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("name", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("description");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("description", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("resourceNameOrId");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.or(
                        Restrictions.like("resourceId", queryValue, MatchMode.ANYWHERE).ignoreCase(),
                        Restrictions.like("name", queryValue, MatchMode.ANYWHERE).ignoreCase()
                        ));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        detachedCriteria.addOrder(Order.asc("resourceId"));
        
        return this.findPageBy(detachedCriteria, pageIndex, pageSize);
    }

    @Override
    public List<Resource> findAllResourcesInOrder() {
        List<Resource> result = this.findAll();
        Comparator<Resource> c = new Comparator<Resource>(){

            @Override
            public int compare(Resource o1, Resource o2) {
                return PinYinTool.toPinYinString(o1.getName()).compareTo(PinYinTool.toPinYinString(o2.getName()));
            }
            
        };        

        Collections.sort(result, c);
        return result;
    }

}
