package net.eulerframework.web.module.authentication.dao.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import net.eulerframework.common.util.Assert;
import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.extend.hibernate5.RestrictionsX;
import net.eulerframework.web.module.authentication.dao.IUserDao;
import net.eulerframework.web.module.authentication.entity.Group;
import net.eulerframework.web.module.authentication.entity.User;

public class UserDao extends BaseDao<User> implements IUserDao {

    @Override
    public User findUserByName(String username) {
        if(username == null)
            return null;
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.eq("username", username));
        List<User> users = this.findBy(detachedCriteria);
        if (users == null || users.isEmpty())
            return null;
        return users.get(0);
    }

    @Override
    public User findUserByEmail(String email) {
        Assert.isNotNull(email, "Email is null");
        
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.eq("email", email));
        List<User> users = this.findBy(detachedCriteria);
        if (users == null || users.isEmpty())
            return null;
        return users.get(0);
    }

    @Override
    public User findUserByMobile(String mobile) {
        if(mobile == null)
            return null;
        
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.eq("mobile", mobile));
        List<User> users = this.findBy(detachedCriteria);
        if (users == null || users.isEmpty())
            return null;
        return users.get(0);
    }

    @Override
    public User findUserByResetToken(String resetToken) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.eq("resetToken", resetToken));
        detachedCriteria.add(Restrictions.gt("resetTokenExpireTime", new Date()));
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
                detachedCriteria.add(RestrictionsX.like("username", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("empName");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(RestrictionsX.like("empName", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("enabled");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.eq("enabled", Boolean.parseBoolean(queryValue)));
            }
            queryValue = queryRequest.getQueryValue("nation");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(RestrictionsX.like("nation", queryValue, MatchMode.ANYWHERE).ignoreCase());
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
