package net.eulerframework.web.module.authentication.service;

import net.eulerframework.web.module.authentication.entity.IUserProfile;

//@PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('AUTH_ADMIN','ADMIN','ROOT')")
public interface IUserProfileService {

    public void saveUserProfile(IUserProfile userProfile);
    
    public IUserProfile loadUserProfile(String userId);
}
