package net.eulerframework.web.module.authentication.service;

import java.util.Collection;

import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.dao.IUserProfileDao;
import net.eulerframework.web.module.authentication.entity.AbstractUserProfile;

public class UserProfileService extends BaseService implements IUserProfileService {

    private Collection<IUserProfileDao<AbstractUserProfile>> userProfileDaos;
    
    public void setUserProfileDaos(Collection<IUserProfileDao<AbstractUserProfile>> userProfileDaos) {
        this.userProfileDaos = userProfileDaos;
    }

    @Override
    public <T extends AbstractUserProfile> void saveUserProfile(T userProfile) {        
        this.getEntityDao(this.userProfileDaos, userProfile.getClass()).save(userProfile);
    }

    @Override
    public <T extends AbstractUserProfile> void updateUserProfile(T userProfile) {        
        this.getEntityDao(this.userProfileDaos, userProfile.getClass()).saveOrUpdate(userProfile);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AbstractUserProfile> T loadUserProfile(String userId, Class<T> clazz) {
        AbstractUserProfile data = this.getEntityDao(this.userProfileDaos, clazz).load(userId);
        
        if(data == null)
            return null;
        
        return (T) data;
    }


}
