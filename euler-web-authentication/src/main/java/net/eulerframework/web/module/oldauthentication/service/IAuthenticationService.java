package net.eulerframework.web.module.oldauthentication.service;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.module.oldauthentication.entity.AbstractUserProfile;
import net.eulerframework.web.module.oldauthentication.entity.User;
import net.eulerframework.web.module.oldauthentication.exception.InvalidEmailResetTokenException;
import net.eulerframework.web.module.oldauthentication.exception.InvalidSMSResetCodeException;
import net.eulerframework.web.module.oldauthentication.exception.UserNotFoundException;

public interface IAuthenticationService extends IBaseService {
 
    public String signUp(User user);
   
    public <T extends AbstractUserProfile> String signUp(User user, T userProfile);

    public void update(User user) throws UserNotFoundException;
    
    public <T extends AbstractUserProfile> void update(User user, T userProfile) throws UserNotFoundException;
    
    @PreAuthorize("isFullyAuthenticated()")
    public void changePassword(String oldPassword, String newPassword);

    public void passwdResetEmailGen(String email);
    
    public void checkEmailResetToken(String token) throws InvalidEmailResetTokenException;
    
    public void resetPasswordByEmailResetToken(String token, String password) throws InvalidEmailResetTokenException, UserNotFoundException;

    public void passwdResetSMSGen(String mobile);
    
    public void resetPasswordBySMSResetCode(String code, String password) throws InvalidSMSResetCodeException, UserNotFoundException;
    
}
