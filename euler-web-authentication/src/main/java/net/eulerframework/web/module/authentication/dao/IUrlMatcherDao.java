package net.eulerframework.web.module.authentication.dao;

import java.util.List;

import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.module.authentication.entity.UrlMatcher;

public interface IUrlMatcherDao extends IBaseDao<UrlMatcher> {

    List<UrlMatcher> findUrlMatcherAuthorities();
    
}
