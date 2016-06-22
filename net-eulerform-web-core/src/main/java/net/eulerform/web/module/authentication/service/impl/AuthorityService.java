package net.eulerform.web.module.authentication.service.impl;

import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.module.authentication.dao.IAuthorityDao;
import net.eulerform.web.module.authentication.dao.IGroupDao;
import net.eulerform.web.module.authentication.entity.Authority;
import net.eulerform.web.module.authentication.entity.Group;
import net.eulerform.web.module.authentication.service.IAuthorityService;

public class AuthorityService extends BaseService implements IAuthorityService {
    
    private IGroupDao groupDao;
    private IAuthorityDao authorityDao;

    public void setGroupDao(IGroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public void setAuthorityDao(IAuthorityDao authorityDao) {
        this.authorityDao = authorityDao;
    }

    @Override
    public void createAuthority(String authority, String description) {
        Authority newAuthority = new Authority();
        newAuthority.setAuthority(authority);
        newAuthority.setDescription(description);
        this.authorityDao.save(newAuthority);        
    }

    @Override
    public void createGroup(String name, String description) {
        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        this.groupDao.save(group);
        
    }   
}
