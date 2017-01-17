package net.eulerframework.web.module.authentication.dao.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import net.eulerframework.common.util.PinYinTool;
import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.extend.hibernate5.RestrictionsX;
import net.eulerframework.web.module.authentication.dao.IGroupDao;
import net.eulerframework.web.module.authentication.entity.Group;

public class GroupDao extends BaseDao<Group> implements IGroupDao {

    @Override
    public PageResponse<Group> findGroupByPage(PageQueryRequest pageQueryRequest) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass)
                .setFetchMode("authorities", FetchMode.SELECT);
        try {
            String queryValue = null;
            queryValue = pageQueryRequest.getQueryValue("name");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(RestrictionsX.like("name", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = pageQueryRequest.getQueryValue("description");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(RestrictionsX.like("description", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        detachedCriteria.addOrder(Order.asc("name"));        
        
        PageResponse<Group> result = this.findPageBy(detachedCriteria, pageQueryRequest.getPageIndex(), pageQueryRequest.getPageSize());
        
        return result;
    }

    @Override
    public List<Group> findAllGroupsInOrder() {
        List<Group> result = this.findAll();

        Comparator<Group> c = new Comparator<Group>() {  
            @Override  
            public int compare(Group o1, Group o2) {  
                return PinYinTool.toPinYinString(o1.getName()).compareTo(PinYinTool.toPinYinString(o2.getName()));  
            }
        };
        
        Collections.sort(result, c);
        return result;
    }

    @Override
    public Group findSystemUsersGroup() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass)
                .setFetchMode("authorities", FetchMode.SELECT);
        detachedCriteria.add(Restrictions.eq("name", Group.SYSTEM_USERS_CROUP_NAME));
        List<Group> result = this.findBy(detachedCriteria);
        if(result == null || result.isEmpty())
            throw new RuntimeException("System Users Group Not Found");
        return result.get(0);
    }

}
