package net.eulerform.web.core.security.authentication.service.impl;

import java.util.List;

import net.eulerform.web.core.base.service.impl.BaseSecurityService;
import net.eulerform.web.core.security.authentication.dao.IAuthorityDao;
import net.eulerform.web.core.security.authentication.dao.IUrlMatcherDao;
import net.eulerform.web.core.security.authentication.entity.Authority;
import net.eulerform.web.core.security.authentication.entity.UrlMatcher;
import net.eulerform.web.core.security.authentication.service.IAuthorityService;

public class AuthorityService extends BaseSecurityService implements IAuthorityService {
    
    private IUrlMatcherDao urlMatcherDao;
    private IAuthorityDao authorityDao;

    public void setUrlMatcherDao(IUrlMatcherDao urlMatcherDao) {
        this.urlMatcherDao = urlMatcherDao;
    }

    public void setAuthorityDao(IAuthorityDao authorityDao) {
        this.authorityDao = authorityDao;
    }

    @Override
    public List<UrlMatcher> findUrlMatcherAuthorities() {
        List<UrlMatcher> returnList =  this.urlMatcherDao.findUrlMatcherAuthorities();        
        if(returnList == null|| returnList.isEmpty()) return null;
        
        for(UrlMatcher urlMatcher : returnList){
            urlMatcher.getAuthorities().size();
        }
        
        return returnList;
    }

    @Override
    public void createAuthority(String authority, String description) {
        Authority newAuthority = new Authority();
        newAuthority.setAuthority(authority);
        newAuthority.setDescription(description);
        this.authorityDao.save(newAuthority);        
    }

    @Override
    public void createUrlMatcher(String urlMatcher, int order) {
        UrlMatcher newUrlMatcher = new UrlMatcher();
        newUrlMatcher.setUrlMatcher(urlMatcher);
        newUrlMatcher.setOrder(order);
        this.urlMatcherDao.save(newUrlMatcher);
    }    
}
