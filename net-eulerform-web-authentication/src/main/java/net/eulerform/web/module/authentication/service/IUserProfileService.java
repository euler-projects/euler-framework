package net.eulerform.web.module.authentication.service;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerform.web.module.authentication.entity.IUserProfile;

@PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('AUTH_ADMIN','ADMIN','SYSTEM')")
public interface IUserProfileService {

    public void saveUserProfile(IUserProfile userProfile);
    
    public IUserProfile loadUserProfile(String userId);
}
