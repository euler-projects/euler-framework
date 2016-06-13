package net.eulerform.web.module.authentication.service.impl;

import java.util.List;

import net.eulerform.web.module.authentication.dao.IGroupDao;
import net.eulerform.web.module.authentication.entity.Group;
import net.eulerform.web.core.base.service.impl.BaseSecurityService;
import net.eulerform.web.module.authentication.dao.IAuthorityDao;
import net.eulerform.web.module.authentication.dao.IUrlMatcherDao;
import net.eulerform.web.module.authentication.entity.Authority;
import net.eulerform.web.module.authentication.entity.UrlMatcher;
import net.eulerform.web.module.authentication.service.IAuthorityService;

public class AuthorityService extends BaseSecurityService implements IAuthorityService {

    private IGroupDao groupDao;
    private IAuthorityDao authorityDao;
    private IUrlMatcherDao urlMatcherDao;

    public void setGroupDao(IGroupDao groupDao) {
        this.groupDao = groupDao;
    }

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

    @Override
    public void createGroup(String name, String description) {
        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        this.groupDao.save(group);
    }    
}
