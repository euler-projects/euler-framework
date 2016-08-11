package net.eulerform.web.module.authentication.dao.impl;

import java.util.List;
import java.util.Set;

import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import net.eulerform.common.StringTool;
import net.eulerform.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerform.web.core.base.entity.PageResponse;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.module.authentication.dao.IUserDao;
import net.eulerform.web.module.authentication.entity.Group;
import net.eulerform.web.module.authentication.entity.User;

public class UserDao extends BaseDao<User> implements IUserDao {

    @Override
    public User findUserByName(String username) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.add(Restrictions.eq("username", username));
        List<User> users = this.findBy(detachedCriteria);
        if (users == null || users.isEmpty())
            return null;
        return users.get(0);
    }

    @Override
    public List<User> findUserByNameOrCode(String nameOrCode) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.add(Restrictions.or(Restrictions.like("username", nameOrCode, MatchMode.ANYWHERE).ignoreCase(),
                Restrictions.like("empName", nameOrCode, MatchMode.ANYWHERE).ignoreCase()));
        return this.findBy(detachedCriteria);
    }

    @Override
    public PageResponse<User> findUserByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass)
                //.setFetchMode("authorities", FetchMode.SELECT)
                .setFetchMode("groups", FetchMode.SELECT);
        try {
            String queryValue = null;
            queryValue = queryRequest.getQueryValue("username");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("username", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("empName");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("empName", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("enabled");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.eq("enabled", Boolean.parseBoolean(queryValue)));
            }
            queryValue = queryRequest.getQueryValue("accountNonExpired");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.eq("accountNonExpired", Boolean.parseBoolean(queryValue)));
            }
            queryValue = queryRequest.getQueryValue("accountNonLocked");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.eq("accountNonLocked", Boolean.parseBoolean(queryValue)));
            }
            queryValue = queryRequest.getQueryValue("credentialsNonExpired");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.eq("credentialsNonExpired", Boolean.parseBoolean(queryValue)));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        detachedCriteria.addOrder(Order.asc("username"));
        
        PageResponse<User> result = this.findPageBy(detachedCriteria, pageIndex, pageSize);
        
        List<User> users = result.getRows();

        for(User user : users){
            user.eraseCredentials();
            user.setAuthorities(null);
            Set<Group> groups = user.getGroups();
            for(Group group : groups){
                group.setAuthorities(null);
            }
        }
        
        return result;
    }
}
