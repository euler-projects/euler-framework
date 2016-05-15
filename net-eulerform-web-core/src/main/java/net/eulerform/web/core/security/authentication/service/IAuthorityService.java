package net.eulerform.web.core.security.authentication.service;

import java.util.List;

import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.core.security.authentication.entity.UrlMatcher;

public interface IAuthorityService extends IBaseService {

    public List<UrlMatcher> findUrlMatcherAuthorities();

    public void createAuthority(String authority, String description);

    public void createUrlMatcher(String urlMatcher, int order);
}
