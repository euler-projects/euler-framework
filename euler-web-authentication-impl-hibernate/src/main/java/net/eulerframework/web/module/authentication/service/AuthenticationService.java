package net.eulerframework.web.module.authentication.service;

import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.eulerframework.common.util.jwt.InvalidJwtException;
import net.eulerframework.common.util.jwt.JwtEncryptor;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.context.UserContext;
import net.eulerframework.web.module.authentication.entity.AbstractUserProfile;
import net.eulerframework.web.module.authentication.entity.Group;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.InvalidEmailResetTokenException;
import net.eulerframework.web.module.authentication.exception.InvalidSMSResetCodeException;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.vo.UserResetInfoVo;
import net.eulerframework.web.util.ServletUtils;

@Service
@Transactional
public class AuthenticationService extends BaseService implements IAuthenticationService {

    @Resource
    private UserService userService;
    @Resource
    private IUserProfileService userProfileService;
    @Resource
    private AuthorityService authorityService;
    private boolean enableAutoAuthorize = WebConfig.isEnableAutoAuthorizeAfterSignup();
    private String[] autoAuthorizeGroupId = WebConfig.getAutoAuthorizeGroupId();
    
    @Resource private JwtEncryptor jwtEncryptor;

    @Override
    public String signUp(User user) {
        user.setId(null);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);

        if (this.enableAutoAuthorize) {
            List<Group> groups = this.authorityService.findGroupByIds(autoAuthorizeGroupId);
            user.setGroups(new HashSet<>(groups));
        }

        return this.userService.save(user);
    }

    @Override
    public <T extends AbstractUserProfile> String signUp(User user, T userProfile) {

        String userId = this.signUp(user);

        userProfile.setUserId(userId);
        this.userProfileService.saveUserProfile(userProfile);

        return userId;
    }

    @Override
    public void update(User user) throws UserNotFoundException {
        String userId = user.getId();
        this.userService.updateFullname(userId, user.getFullName());
        this.userService.updateMobile(userId, user.getMobile());
        
    }

    @Override
    public <T extends AbstractUserProfile> void update(User user, T userProfile) throws UserNotFoundException {
        if(!user.getId().equals(userProfile.getUserId()))
            throw new RuntimeException("userProfile's userId must equals with user's id");
        
        this.update(user);
        this.userProfileService.updateUserProfile(userProfile);        
    }

    @Override
    public void passwdResetEmailGen(String email) {
        try {
            User user = this.userService.loadUserByEmail(email);
            
            if(user == null)
                throw new UserNotFoundException("User email is '" + email + "' not found");
            
            UserResetInfoVo vo = new UserResetInfoVo(user, 10 * 60);
            
            String token = this.jwtEncryptor.encode(vo).getEncoded();
            
            String resetUrl = ServletUtils.getWebDomain() + ServletUtils.getServletContext().getContextPath() + "/reset-password?type=email&token=" + token;
            System.out.println(resetUrl);
            // TODO send email
        } catch (Exception e) {
            if(WebConfig.isDebugMode()) {
                this.logger.error("passwdResetEmailGen error" ,e);
            } else {
                //DO_NOTHING            
            }
        }

    }

    @Override
    public void checkEmailResetToken(String token) throws InvalidEmailResetTokenException {
        try {            
            this.jwtEncryptor.decode(token);            
        } catch (InvalidJwtException e) {
            throw new InvalidEmailResetTokenException(e.getMessage(), e);
        }
    }

    @Override
    public void resetPasswordByEmailResetToken(String token, String password) throws InvalidEmailResetTokenException, UserNotFoundException {
        try {
            UserResetInfoVo vo = this.jwtEncryptor.decode(token, UserResetInfoVo.class);
            
            this.userService.updateUserPasswordWithoutCheck(vo.getId(), password);
        } catch (InvalidJwtException e) {
            throw new InvalidEmailResetTokenException(e.getMessage(), e);
        }
        
    }

    @Override
    public void passwdResetSMSGen(String mobile) {
        // TODO Auto-generated method stub

    }

    @Override
    public void resetPasswordBySMSResetCode(String code, String password)
            throws InvalidSMSResetCodeException, UserNotFoundException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        String userId = UserContext.getCurrentUser().getId();

        try {
            this.userService.updateUserPassword(userId, oldPassword, newPassword);
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
