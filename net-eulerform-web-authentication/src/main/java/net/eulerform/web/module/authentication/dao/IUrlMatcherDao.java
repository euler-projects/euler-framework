package net.eulerform.web.module.authentication.dao;

import java.util.List;

import net.eulerform.web.core.base.dao.IBaseDao;
import net.eulerform.web.module.authentication.entity.UrlMatcher;

public interface IUrlMatcherDao extends IBaseDao<UrlMatcher> {

    List<UrlMatcher> findUrlMatcherAuthorities();
    
}
