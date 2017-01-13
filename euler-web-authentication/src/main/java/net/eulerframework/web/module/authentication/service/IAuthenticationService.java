package net.eulerframework.web.module.authentication.service;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.module.authentication.entity.AbstractUserProfile;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;

public interface IAuthenticationService extends IBaseService {
 
    public String signUp(User user);
   
    public <T extends AbstractUserProfile> String signUp(User user, T userProfile);

    public void update(User user) throws UserNotFoundException;
    
    public <T extends AbstractUserProfile> void update(User user, T userProfile) throws UserNotFoundException;
    
    @PreAuthorize("isFullyAuthenticated()")
    public void changePassword(String oldPassword, String newPassword);

    public void passwdResetEmailGen(String email);

    public void passwdResetSMSGen(String mobile);
    
}
