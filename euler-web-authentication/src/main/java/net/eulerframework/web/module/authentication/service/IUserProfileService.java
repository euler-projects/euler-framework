package net.eulerframework.web.module.authentication.service;

import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.module.authentication.entity.AbstractUserProfile;

public interface IUserProfileService extends IBaseService {

    public <T extends AbstractUserProfile> void saveUserProfile(T userProfile);
    
    public AbstractUserProfile loadUserProfile(String userId);
}
