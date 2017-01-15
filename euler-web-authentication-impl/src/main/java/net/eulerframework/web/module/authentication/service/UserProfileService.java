package net.eulerframework.web.module.authentication.service;

import java.util.Set;

import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.dao.IUserProfileDao;
import net.eulerframework.web.module.authentication.entity.AbstractUserProfile;

public class UserProfileService extends BaseService implements IUserProfileService {

    private Set<IUserProfileDao<AbstractUserProfile>> userProfileDaos;
    
    public void setUserProfileDaos(Set<IUserProfileDao<AbstractUserProfile>> userProfileDaos) {
        this.userProfileDaos = userProfileDaos;
    }

    @Override
    public <T extends AbstractUserProfile> void saveUserProfile(T userProfile) {
        for(IUserProfileDao<AbstractUserProfile> userProfileDao : this.userProfileDaos) {
            if(userProfileDao.isMyEntity(userProfile.getClass())) {
                userProfileDao.save(userProfile);
                return;
            }                        
        }
        throw new RuntimeException("Cannot find dao for " + userProfile.getClass().getName());       
    }

    @Override
    public <T extends AbstractUserProfile> void updateUserProfile(T userProfile) {
        for(IUserProfileDao<AbstractUserProfile> userProfileDao : this.userProfileDaos) {
            if(userProfileDao.isMyEntity(userProfile.getClass())) {
                userProfileDao.saveOrUpdate(userProfile);
                return;
            }
        }
        throw new RuntimeException("Cannot find dao for " + userProfile.getClass().getName());   
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AbstractUserProfile> T loadUserProfile(String userId, Class<T> clazz) {
        for(IUserProfileDao<AbstractUserProfile> userProfileDao : this.userProfileDaos) {
            if(userProfileDao.isMyEntity(clazz))
                return (T) userProfileDao.load(userId);
        }
        throw new RuntimeException("Cannot find dao for " + clazz.getName());   
    }


}
