package net.eulerframework.web.module.authentication.service;

import org.springframework.transaction.annotation.Transactional;

import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.module.authentication.entity.IUserProfile;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.UserSignUpException;

@Transactional
public interface IAuthenticationService extends IBaseService {
 
    public String signUp(User user) throws UserSignUpException;
   
    public <T extends IUserProfile> String signUp(User user, T userProfile) throws UserSignUpException;

    public void passwdResetEmailGen(String email);

    public void passwdResetSMSGen(String email);

    public User findUser(String passwordResetToken);
    
}
