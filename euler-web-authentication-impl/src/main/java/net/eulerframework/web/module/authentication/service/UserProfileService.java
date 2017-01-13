package net.eulerframework.web.module.authentication.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.dao.IUserProfileDao;
import net.eulerframework.web.module.authentication.entity.AbstractUserProfile;

@Service
public class UserProfileService extends BaseService implements IUserProfileService {

    @Resource
    private IUserProfileDao<AbstractUserProfile> userProfileDao;
    
    @Override
    public <T extends AbstractUserProfile> void saveUserProfile(T userProfile) {
        this.userProfileDao.save(userProfile);        
    }

    @Override
    public AbstractUserProfile loadUserProfile(String userId) {
        return this.userProfileDao.load(userId);
    }

}
