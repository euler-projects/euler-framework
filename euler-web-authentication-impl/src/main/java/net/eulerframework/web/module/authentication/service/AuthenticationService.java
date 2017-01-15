package net.eulerframework.web.module.authentication.service;

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
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.util.JwtEncryptor;
import net.eulerframework.web.module.authentication.util.UserContext;

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
            
            JwtEncryptor j = new JwtEncryptor();
            j.setSigningKey(jwtSigningKey());
            j.setVerifierKey(jwtVerifierKey());
            
            String token = j.encode(vo).getEncoded();
            
            System.out.println("!!!!!!!!!!Reset token: " + token);
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
    public void resetPasswordByResetToken(String token) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void passwdResetSMSGen(String mobile) {
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
    
    public class UserResetInfoVo {
        private String id;
        private String username;
        private Date genDate;
        private Date expireDate;
        
        public UserResetInfoVo(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.genDate = new Date();
            this.expireDate = new Date(this.genDate.getTime() + 1 * 60 * 60 * 1000);
        }
        
        public String getId() {
            return id;
        }
        public String getUsername() {
            return username;
        }
        public Date getGenDate() {
            return genDate;
        }
        public Date getExpireDate() {
            return expireDate;
        }        
    }
    
    public String jwtSigningKey() {
        return "-----BEGIN RSA PRIVATE KEY-----MIIEpgIBAAKCAQEAobqoV5brW/wVskAimNXSB+v+9NHpWF3wq29ZQJhyMEIcMAHzhbfVSZgsDmpGEJTP/eH/tX0k2FoFFkfj3U9cXGIJZVjeNTPDKC7BTtmU/ZfmR1xoJmDexqALa3KRFxvCuUZtAxk75gMmm1+VSDDwDDGsfz7LTsKxUhcXM0A8Oh/w02xghsTuqn2vfKnE3LInZOs785ntoocDlZcexsX4bLXdjrK02YGcqSLavieYI8fjxcfhCD1CGOmUrA6xgADVTs5wBfK+Kg1mYPnYAv6yuWPv3k2WdtsZ7JsT639NksMyOYsxXeQWut9Xj2eeDwGoagEX1s+snhc4CImW6LtWMwIDAQABAoIBAQCE6SRdz06fKr0d321PUzGnhv/hbP0qvREDop+j4WS+WiZWIdRjCSAEukVCl337NID2MZv3J+B22QwjMnOGNik+VudH3c/Hw0FYLYx544B5JDOAY+XH3IZYj8CyzdWFOzA9GS6PhFZggihhOh0x1d4A93W+oPluQbx+LTHI0bptPPIxuXTEpUegi1XlbfU0ZBqW9TtG2XiS9vPKpVQ7KMTJSVdxjXCOaKNSDChEHKfSLw9n0Y6qEh/teNvhckzCtBdDt4rZwZptklWmxFX7zU1TlDU1OqCSlDF5KPIyNGLA/2lYxOqqG3V4m++oATtpp4l0KVw496DV/8kq4+ZG+iQZAoGBAM4sLHwo6PfFd82ys0pn/MdcTJA+GDDkiO94jnaA9CKit+HTXFw8WKjGjlBafr3SfTWwO9ig8fgekGu19VIEtVGc+dzQYBYl6VPdEiM4NKGHvFutlDP69e8/3ZKupow3jwCTzhfgsIjgJA9IWLcp4npA9f1DpmtwyFZZPVwOQ2pPAoGBAMjQyMaWcMZZ/Rw+ig+JbkcPcQGPX5pWNuTm+vurUYdfC6x2x0QpbK+2TWdGx6E5NAwmTVJ7rm4jFqvWgQnJevN3RvdotvIcuucs786I2PVLJKnwTPw6eon7uKyHVw+ddke176giwzhaui2ny32h2v23twEk2JDnQ0JjoJVSGnDdAoGBAIn7bGZIJuq0QOLsxytz/uwZ7K/Yru4B9Vd3srjCwyFvD2vWvgiI5rlF8bb7abl25w+Ie/UWefqZ0gQUSjPzLLqLOXo8ByKrisXyvZHOqwK0Si59NCO5wOC3OH5T3ukWweEcCqFWYi+o+tkzjRRAtu8lDLzMitN7LskDfppefWXnAoGBAIZZ2Nmz8MNjlUl+NdPrOFJmbE6E44tYPuWp+yTBG4yb9C1wUiSyKjrslqCP5CNjKAUw4u5aPPsGkrZojnBD0fRtSpdgAXW97vWXROFDARQrL95aHMdrQGxscsNK0N5rlKSpfitZBo7/dCvzZNsqnF6+uLsVMabQcllKWjdMdNApAoGBAIEyI+iCbKZonaNZ6x8LrVwLYDvio1ExGpzb+Wj5xpXwZNhfKua4uocOs+42BQfqXzKMCmjEVSiNs+n5HRYkkF2yN9DA6SBy/aJvM2Wgv5NF6aWVD1SDAHFRQYwD94InpUMNT0EOp9FBJy7tNSEt2nRC/yN5hkfivpN7BxUporH9-----END RSA PRIVATE KEY-----";
    }

    public String jwtVerifierKey() {
        //return "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7edY0ZALoT+WnhEOmpmSOqQbv2xcChMJwLCtT/LDSXJtOZagjEzodLBI6jUrRDfQ7YU0TLygGWvL+o9ZiWnJQL7UWZO2y66YbAwaUI0FS4uorAebyqbt98mAa41x8PqBqd8pwWSgB6OsYu+bRts1NfYtNOPwVYUyCbT+rL7q1Z1cx8yi3fRRCXp0/bmD4gNmN1S0eHNFOkiCv5/8/CK3nXwzRqUojftbmv52PcPMf3Q6XOeQZBxV4ynoKvri788uV4l9A1iNIf7/lgEmTlU72s+3kx/5fhXerjHSmdZ2/pGDN8Q/+xXKA/2smXmtzx2ZTvFYuzyb64yqEq7CuAVgXwIDAQAB-----END PUBLIC KEY-----";
        return "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAobqoV5brW/wVskAimNXSB+v+9NHpWF3wq29ZQJhyMEIcMAHzhbfVSZgsDmpGEJTP/eH/tX0k2FoFFkfj3U9cXGIJZVjeNTPDKC7BTtmU/ZfmR1xoJmDexqALa3KRFxvCuUZtAxk75gMmm1+VSDDwDDGsfz7LTsKxUhcXM0A8Oh/w02xghsTuqn2vfKnE3LInZOs785ntoocDlZcexsX4bLXdjrK02YGcqSLavieYI8fjxcfhCD1CGOmUrA6xgADVTs5wBfK+Kg1mYPnYAv6yuWPv3k2WdtsZ7JsT639NksMyOYsxXeQWut9Xj2eeDwGoagEX1s+snhc4CImW6LtWMwIDAQAB-----END PUBLIC KEY-----";
    }

}
