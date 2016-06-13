package net.eulerform.web.module.authentication.service;

import java.util.List;

import net.eulerform.web.core.base.service.IBaseSecurityService;
import net.eulerform.web.module.authentication.entity.UrlMatcher;

public interface IAuthorityService extends IBaseSecurityService {

    public List<UrlMatcher> findUrlMatcherAuthorities();

    public void createAuthority(String authority, String description);

    public void createUrlMatcher(String urlMatcher, int order);

    public void createGroup(String name, String description);
}
