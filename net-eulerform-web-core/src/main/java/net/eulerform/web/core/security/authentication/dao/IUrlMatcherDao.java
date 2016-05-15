package net.eulerform.web.core.security.authentication.dao;

import java.util.List;

import net.eulerform.web.core.base.dao.hibernate5.IBaseDao;
import net.eulerform.web.core.security.authentication.entity.UrlMatcher;

public interface IUrlMatcherDao extends IBaseDao<UrlMatcher> {

    List<UrlMatcher> findUrlMatcherAuthorities();
    
}
