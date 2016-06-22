package net.eulerform.web.module.authentication.dao.impl;

import java.util.List;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import net.eulerform.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerform.web.module.authentication.dao.IUserDao;
import net.eulerform.web.module.authentication.entity.User;

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

    @Override
    public List<User> findUserByNameOrCode(String nameOrCode) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.add(Restrictions.or(Restrictions.like("username", nameOrCode, MatchMode.ANYWHERE).ignoreCase(),
                Restrictions.like("empName", nameOrCode, MatchMode.ANYWHERE).ignoreCase()));
        return this.findBy(detachedCriteria);
    }
}
