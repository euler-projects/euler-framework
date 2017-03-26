package net.eulerframework.web.module.authentication.dao;

import java.util.List;
import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import net.eulerframework.common.util.StringUtils;
import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.core.base.request.easyuisupport.EasyUiQueryReqeuset;
import net.eulerframework.web.core.base.response.easyuisupport.EasyUIPageResponse;
import net.eulerframework.web.core.extend.hibernate5.RestrictionsX;
import net.eulerframework.web.module.authentication.entity.User;

public class UserDao extends BaseDao<User> {
    
    public User findUserByName(String username) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.eq("username", username));
        List<User> users = this.query(detachedCriteria);
        if (users == null || users.isEmpty())
            return null;
        return users.get(0);
    }

    
    public User findUserByEmail(String email) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.eq("email", email));
        List<User> users = this.query(detachedCriteria);
        if (users == null || users.isEmpty())
            return null;
        return users.get(0);
    }

    
    public User findUserByMobile(String mobile) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.eq("mobile", mobile));
        List<User> users = this.query(detachedCriteria);
        if (users == null || users.isEmpty())
            return null;
        return users.get(0);
    }

    
    public List<User> findUserByNameOrCode(String nameOrCode) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.add(Restrictions.or(Restrictions.like("username", nameOrCode, MatchMode.ANYWHERE).ignoreCase(),
                Restrictions.like("empName", nameOrCode, MatchMode.ANYWHERE).ignoreCase()));
        return this.query(detachedCriteria);
    }

    
    public EasyUIPageResponse<User> findUserByPage(EasyUiQueryReqeuset queryRequest) {        
        
        DetachedCriteria detachedCriteria = this.analyzeQueryRequest(queryRequest);
        
        detachedCriteria.setFetchMode("groups", FetchMode.SELECT);

        String filterValue = null;
        filterValue = queryRequest.getFilterValue("groupId");
        if(StringUtils.hasText(filterValue)) {
            @SuppressWarnings("unchecked")
            List<String> userIdList = this.getCurrentSession().createSQLQuery("select USER_ID from SYS_USER_GROUP where GROUP_ID = :groupId").setString("groupId", filterValue).list();
            
            if(userIdList == null || userIdList.isEmpty())
                detachedCriteria.add(RestrictionsX.in("id", new String[] {""}));
            else
                detachedCriteria.add(RestrictionsX.in("id", userIdList));
        }
        filterValue = queryRequest.getFilterValue("enabled");
        if(!StringUtils.isEmpty(filterValue)) {
            detachedCriteria.add(Restrictions.eq("enabled", Boolean.parseBoolean(filterValue)));            
        }
        
        int pageIndex = queryRequest.getPageIndex();
        int pageSize = queryRequest.getPageSize();
        
        EasyUIPageResponse<User> ret = this.pageQuery(detachedCriteria, pageIndex, pageSize);  
        for(User user : ret.getRows()){
            user.eraseCredentials();
        }
        
        return ret;
    }
}
