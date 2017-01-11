package net.eulerframework.web.module.authentication.service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.eulerframework.common.util.Assert;
import net.eulerframework.common.util.BeanTool;
import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.core.exception.BadRequestException;
import net.eulerframework.web.module.authentication.dao.IUserProfileDao;
import net.eulerframework.web.module.authentication.entity.Group;
import net.eulerframework.web.module.authentication.entity.IUserProfile;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.PasswordException;
import net.eulerframework.web.module.authentication.exception.UserChangePasswordException;
import net.eulerframework.web.module.authentication.exception.UserEmailException;
import net.eulerframework.web.module.authentication.exception.UserMobileException;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.exception.UserSignUpException;
import net.eulerframework.web.module.authentication.exception.UsernameException;
import net.eulerframework.web.module.authentication.util.UserContext;

@Service
@Transactional
public class AuthenticationService extends BaseService implements IAuthenticationService {

    @Resource
    private IUserService userService;
    @Resource
    private IUserProfileDao<IUserProfile> userProfileDao;
    @Resource
    private IAuthorityService authorityService;
    @Resource
    private PasswordEncoder passwordEncoder;
    private boolean autoAuthorization = WebConfig.getAutoAuthorization();
    private String[] autoAuthorizationGroupId = WebConfig.getAutoAuthorizationId();

    @Override
    public String signUp(User user) throws UserSignUpException {

        try {
            BeanTool.clearEmptyProperty(user);

            String password;

            try {
                Assert.isNotNull(user.getUsername(), BadRequestException.class, UsernameException.INFO.USERNAME_IS_NULL.toString());
                Assert.isNotNull(user.getEmail(), BadRequestException.class, UserEmailException.INFO.EMAIL_IS_NULL.toString());
                Assert.isNotNull(user.getPassword(), BadRequestException.class, PasswordException.INFO.PASSWD_IS_ULL.toString());
                // Assert.isNotNull(user.getMobile(), BadRequestException.class,
                // "Mobile is null"));

                Assert.isTrue(user.getUsername().matches(WebConfig.getUsernameFormat()), BadRequestException.class,
                        UsernameException.INFO.INCORRECT_USERNAME_FORMAT.toString());
                Assert.isTrue(user.getEmail().matches(WebConfig.getEmailFormat()), BadRequestException.class,
                        UserEmailException.INFO.INCORRECT_EMAIL_FORMAT.toString());

                Assert.isNull(this.userService.loadUserByUsername(user.getUsername()), BadRequestException.class,
                        UsernameException.INFO.USERNAME_USED.toString());
                Assert.isNull(this.userService.loadUserByEmail(user.getEmail()), BadRequestException.class,
                        UserEmailException.INFO.EMAIL_USED.toString());

                if (user.getMobile() != null)
                    Assert.isNull(this.userService.loadUserByMobile(user.getMobile()), BadRequestException.class,
                            UserMobileException.INFO.MOBILE_USED.toString());

                password = user.getPassword().trim();
                Assert.isTrue(password.matches(WebConfig.getPasswordFormat()), BadRequestException.class,
                        PasswordException.INFO.INCORRECT_PASSWD_FORMAT.toString());
                Assert.isTrue(password.length() >= WebConfig.getMinPasswordLength() && password.length() <= 20,
                        BadRequestException.class, PasswordException.INFO.INCORRECT_PASSWD_LENGTH.toString());

            } catch (BadRequestException e) {
                throw new UserSignUpException(e.getMessage(), e);
            }

            user.setId(null);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            user.setEnabled(true);

            user.setPassword(this.passwordEncoder.encode(password));

            user.setSignUpTime(new Date());

            if (this.autoAuthorization) {
                List<Group> groups = this.authorityService.findGroupByIds(autoAuthorizationGroupId);
                user.setGroups(new HashSet<>(groups));
            }

            return this.userService.save(user);
        } catch (UserSignUpException userSignUpException) {
            throw userSignUpException;
        } catch (Exception e) {
            throw new UserSignUpException(UserSignUpException.INFO.UNKNOWN_USER_SIGNUP_ERROR.toString(), e);
        }
    }

    @Override
    public <T extends IUserProfile> String signUp(User user, T userProfile) throws UserSignUpException {

        try {
            String userId = this.signUp(user);

            if (StringTool.isNull(userId))
                throw new Exception();

            userProfile.setUserId(userId);
            this.userProfileDao.save(userProfile);

            return userId;
        } catch (UserSignUpException userSignUpException) {
            throw userSignUpException;
        } catch (Exception e) {
            throw new UserSignUpException(UserSignUpException.INFO.UNKNOWN_USER_SIGNUP_ERROR.toString(), e);
        }
    }

    @Override
    public void passwdResetEmailGen(String email) {
        // TODO Auto-generated method stub

    }

    @Override
    public void passwdResetSMSGen(String email) {
        // TODO Auto-generated method stub

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) throws UserChangePasswordException {
        String userId = UserContext.getCurrentUser().getId();

        User user = this.userService.loadUser(userId);

        if (!this.passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new UserChangePasswordException(PasswordException.INFO.INCORRECT_PASSWD.toString());
        }

        String password;
        try {
            password = newPassword.trim();
            Assert.isTrue(password.matches(WebConfig.getPasswordFormat()), BadRequestException.class,
                    PasswordException.INFO.INCORRECT_PASSWD_FORMAT.toString());
            Assert.isTrue(password.length() >= WebConfig.getMinPasswordLength() && password.length() <= 20,
                    BadRequestException.class, PasswordException.INFO.INCORRECT_PASSWD_LENGTH.toString());
        } catch (BadRequestException e) {
            throw new UserChangePasswordException(e.getMessage(), e);
        }
        
        user.setPassword(this.passwordEncoder.encode(password));
        
        try {
            this.userService.updateUser(user);
        } catch (UserNotFoundException e) {
            throw new UserChangePasswordException(e.getMessage(), e);
        }
    }

}
