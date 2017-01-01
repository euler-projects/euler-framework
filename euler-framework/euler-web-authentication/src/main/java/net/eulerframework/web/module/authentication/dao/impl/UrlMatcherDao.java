package net.eulerframework.web.module.authentication.dao.impl;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.module.authentication.dao.IUrlMatcherDao;
import net.eulerframework.web.module.authentication.entity.UrlMatcher;

public class UrlMatcherDao extends BaseDao<UrlMatcher> implements IUrlMatcherDao {

    @Override
    public List<UrlMatcher> findUrlMatcherAuthorities() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.addOrder(Order.asc("order"));
        return this.findBy(detachedCriteria);
    }
}
