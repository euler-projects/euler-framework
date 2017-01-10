package net.eulerframework.web.module.authentication.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;

import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.module.authentication.entity.IUserProfile;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.UserSignUpException;

public interface IAuthenticationService extends IBaseService {
 
    public String signUp(User user) throws UserSignUpException;
   
    public <T extends IUserProfile> String signUp(User user, T userProfile) throws UserSignUpException;
    
    @PreAuthorize("isFullyAuthenticated()")
    public void changePassword(String oldPassword, String newPassword) throws BadCredentialsException;

    public void passwdResetEmailGen(String email);

    public void passwdResetSMSGen(String email);
    
}
