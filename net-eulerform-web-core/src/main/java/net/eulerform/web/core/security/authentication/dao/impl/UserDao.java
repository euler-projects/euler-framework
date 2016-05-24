package net.eulerform.web.core.security.authentication.dao.impl;

import java.util.List;

import net.eulerform.web.core.base.dao.hibernate5.impl.BaseDao;
import net.eulerform.web.core.security.authentication.dao.IUserDao;
import net.eulerform.web.core.security.authentication.entity.User;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

public class UserDao extends BaseDao<User> implements IUserDao {

    @Override
    public User findUserByName(String username) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.add(Restrictions.eq("username", username));
        List<User> users = this.findBy(detachedCriteria);
        if(users == null || users.isEmpty()) return null;        
        User user =  users.get(0);
        this.getSessionFactory().getCurrentSession().evict(user);
        return user;
    }

}
