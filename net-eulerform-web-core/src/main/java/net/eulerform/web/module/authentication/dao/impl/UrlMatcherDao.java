package net.eulerform.web.module.authentication.dao.impl;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

import net.eulerform.web.core.base.dao.hibernate5.impl.BaseDao;
import net.eulerform.web.module.authentication.dao.IUrlMatcherDao;
import net.eulerform.web.module.authentication.entity.UrlMatcher;

public class UrlMatcherDao extends BaseDao<UrlMatcher> implements IUrlMatcherDao {

    @Override
    public List<UrlMatcher> findUrlMatcherAuthorities() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.addOrder(Order.asc("order"));
        return this.findBy(detachedCriteria);
    }
}
