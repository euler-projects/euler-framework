package net.eulerframework.web.module.authentication.service;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.eulerframework.web.config.ProjectMode;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.entity.AbstractUserProfile;
import net.eulerframework.web.module.authentication.entity.Group;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.InvalidEmailResetTokenException;
import net.eulerframework.web.module.authentication.exception.InvalidJwtException;
import net.eulerframework.web.module.authentication.exception.InvalidSMSResetCodeException;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.util.JwtEncryptor;
import net.eulerframework.web.module.authentication.util.UserContext;
import net.eulerframework.web.module.authentication.vo.UserResetInfoVo;

@Service
@Transactional
public class AuthenticationService extends BaseService implements IAuthenticationService {

    @Resource
    private IUserService userService;
    @Resource
    private IUserProfileService userProfileService;
    @Resource
    private IAuthorityService authorityService;
    private boolean autoAuthorization = WebConfig.getAutoAuthorization();
    private String[] autoAuthorizationGroupId = WebConfig.getAutoAuthorizationId();
    
    @Resource private JwtEncryptor jwtEncryptor;

    @Override
    public String signUp(User user) {

//        try {

//            try {
//                Assert.isNotNull(user.getUsername(), BadRequestException.class, Lang.USERNAME.USERNAME_IS_NULL.toString());
//                Assert.isNotNull(user.getEmail(), BadRequestException.class, Lang.USER_EMAIL.EMAIL_IS_NULL.toString());
//                Assert.isNotNull(user.getPassword(), BadRequestException.class, Lang.PASSWD.PASSWD_IS_ULL.toString());
//                // Assert.isNotNull(user.getMobile(), BadRequestException.class,
//                // "Mobile is null"));
//
//                Assert.isTrue(user.getUsername().matches(WebConfig.getUsernameFormat()), BadRequestException.class,
//                        Lang.USERNAME.INCORRECT_USERNAME_FORMAT.toString());
//                Assert.isTrue(user.getEmail().matches(WebConfig.getEmailFormat()), BadRequestException.class,
//                        Lang.USER_EMAIL.INCORRECT_EMAIL_FORMAT.toString());
//
//                Assert.isNull(this.userService.loadUserByUsername(user.getUsername()), BadRequestException.class,
//                        Lang.USERNAME.USERNAME_USED.toString());
//                Assert.isNull(this.userService.loadUserByEmail(user.getEmail()), BadRequestException.class,
//                        Lang.USER_EMAIL.EMAIL_USED.toString());
//
//                if (user.getMobile() != null)
//                    Assert.isNull(this.userService.loadUserByMobile(user.getMobile()), BadRequestException.class,
//                            Lang.USER_MOBILE.MOBILE_USED.toString());
//
//                password = user.getPassword().trim();
//                Assert.isTrue(password.matches(WebConfig.getPasswordFormat()), BadRequestException.class,
//                        Lang.PASSWD.INCORRECT_PASSWD_FORMAT.toString());
//                Assert.isTrue(password.length() >= WebConfig.getMinPasswordLength() && password.length() <= 20,
//                        BadRequestException.class, Lang.PASSWD.INCORRECT_PASSWD_LENGTH.toString());
//
//            } catch (BadRequestException e) {
//                throw new UserSignUpException(e.getMessage(), e);
//            }

        user.setId(null);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);

        if (this.autoAuthorization) {
            List<Group> groups = this.authorityService.findGroupByIds(autoAuthorizationGroupId);
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
        User existUser = this.userService.loadUser(user.getId());
        
        if(existUser == null)
            throw new UserNotFoundException();
        
        user.setAccountNonExpired(existUser.isAccountNonExpired());
        user.setAccountNonLocked(existUser.isAccountNonLocked());
        user.setCredentialsNonExpired(existUser.isCredentialsNonExpired());
        user.setEnabled(existUser.isEnabled());
        user.setGroups(existUser.getGroups());
        
        this.userService.updateUser(user);
        
    }

    @Override
    public <T extends AbstractUserProfile> void update(User user, T userProfile) throws UserNotFoundException {
        this.update(user);
        this.userProfileService.updateUserProfile(userProfile);        
    }

    @Override
    public void passwdResetEmailGen(String email) {
        try {
            User user = this.userService.loadUserByEmail(email);
            
            if(user == null)
                throw new UserNotFoundException("User email is '" + email + "' not found");
            
            UserResetInfoVo vo = new UserResetInfoVo(user);
            
            String token = this.jwtEncryptor.encode(vo).getEncoded();
            
            System.out.println("!!!!!!!!!!Reset token: " + token);
            // TODO send email
        } catch (Exception e) {
            if(WebConfig.getProjectMode().equals(ProjectMode.DEVELOP) ||
                    WebConfig.getProjectMode().equals(ProjectMode.DEBUG)) {
                this.logger.error("passwdResetEmailGen error" ,e);
            } else {
                //DO_NOTHING            
            }
        }

    }

    @Override
    public void checkEmailResetToken(String token) throws InvalidEmailResetTokenException {
        try {            
            UserResetInfoVo vo = this.jwtEncryptor.decodeClaims(token, UserResetInfoVo.class);
            Date expireDate = vo.getExpireDate();
            
            if(expireDate.getTime() <= new Date().getTime())
                throw new InvalidJwtException("token expired");
            
        } catch (InvalidJwtException | IOException e) {
            throw new InvalidEmailResetTokenException(e.getMessage(), e);
        }
    }

    @Override
    public void resetPasswordByEmailResetToken(String token, String password) throws InvalidEmailResetTokenException, UserNotFoundException {
        try {
            UserResetInfoVo vo = this.jwtEncryptor.decodeClaims(token, UserResetInfoVo.class);
            
            this.userService.updateUserPasswordWithoutCheck(vo.getId(), password);
        } catch (InvalidJwtException | IOException e) {
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
