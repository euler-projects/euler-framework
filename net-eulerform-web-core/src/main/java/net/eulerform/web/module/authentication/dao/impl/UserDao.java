package net.eulerform.web.module.authentication.dao.impl;

import java.util.List;

import net.eulerform.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerform.web.module.authentication.dao.IUserDao;
import net.eulerform.web.module.authentication.entity.User;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

public class UserDao extends BaseDao<User> implements IUserDao {

    @Override
    public User findUserByName(String username) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.add(Restrictions.eq("username", username));
        detachedCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        List<User> users = this.findBy(detachedCriteria);
        if (users == null || users.isEmpty())
            return null;
        User user = users.get(0);
        return user;
    }
}
