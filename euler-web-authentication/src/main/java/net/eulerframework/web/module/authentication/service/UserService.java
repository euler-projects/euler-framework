package net.eulerframework.web.module.authentication.service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import net.eulerframework.common.util.Assert;
import net.eulerframework.common.util.JavaObjectUtils;
import net.eulerframework.common.util.StringUtils;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.base.request.EasyUiQueryReqeuset;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.dao.GroupDao;
import net.eulerframework.web.module.authentication.dao.UserDao;
import net.eulerframework.web.module.authentication.entity.Group;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.UserAuthenticationViewException;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;

@Service
public class UserService extends BaseService {

    @Resource
    private UserDao userDao;
    @Resource
    private GroupDao groupDao;
    @Resource
    private PasswordEncoder passwordEncoder;

    public User loadUserByUsername(String username) {
        Assert.notNull(username, "username is null");
        User user = this.userDao.findUserByName(username);
        return user;
    }

    public User loadUserByEmail(String email) {
        Assert.notNull(email, "email is null");
        User user = this.userDao.findUserByEmail(email);
        return user;
    }

    public User loadUserByMobile(String mobile) {
        Assert.notNull(mobile, "mobile is null");
        User user = this.userDao.findUserByMobile(mobile);
        return user;
    }

    public List<User> loadUserByNameOrCodeFuzzy(String nameOrCode) {
        Assert.notNull(nameOrCode, "nameOrCode is null");
        return this.userDao.findUserByNameOrCode(nameOrCode);
    }

    public User loadUser(String userId) {
        Assert.notNull(userId, "userId is null");
        return this.userDao.load(userId);
    }

    public String save(User user) {
        Assert.notNull(user, "user is null");
        
        JavaObjectUtils.clearEmptyProperty(user);
        
        user.setUsername(StringUtils.trim(user.getUsername()));
        user.setEmail(StringUtils.trim(user.getEmail()));
        user.setMobile(StringUtils.trim(user.getMobile()));
        user.setPassword(StringUtils.trim(user.getPassword()));
        user.setFullName(StringUtils.trim(user.getFullName()));
        
        this.validPassword(user.getPassword());
        this.validUsername(user.getUsername());
        this.validEmail(user.getEmail());
        this.validMobile(user.getMobile());
        
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        user.setSignUpTime(new Date());

        return (String) this.userDao.save(user);
    }

    public PageResponse<User> findUserByPage(EasyUiQueryReqeuset queryRequest) {
        PageResponse<User> ret = this.userDao.findUserByPage(queryRequest);

        for(User user : ret.getRows()){
            Set<Group> groups = user.getGroups();
            
            if(groups != null) {
                for(Group group : groups) {
                    if(StringUtils.isEmpty(user.getUserGroups()))
                        user.setUserGroups(group.getName());
                    else
                        user.setUserGroups(user.getUserGroups() + "; " +  group.getName());   
                }
            }
        }
        
        return ret;
    }

    /**
     * 修改密码，需要验证旧密码
     * 
     * @param userId
     *            被修改的用户ID
     * @param oldPassword
     *            旧密码
     * @param newPassword
     *            新密码
     * @throws UserNotFoundException
     *             用户不存在
     */
    public void updateUserPassword(String userId, String oldPassword, String newPassword) throws UserNotFoundException {
        User user = this.loadUser(userId);

        if (user == null)
            throw new UserNotFoundException("User id is \"" + userId + "\" not found.");

        if (!this.passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new UserAuthenticationViewException("_INCORRECT_PASSWORD");
        }

        String password = newPassword.trim();
        this.validPassword(password);

        user.setPassword(this.passwordEncoder.encode(password));

        this.userDao.update(user);
    }
    
    public void updateUsername(String userId, String username) throws UserNotFoundException {
        Assert.notNull(username, "username is null");
        username = username.trim();
        User user = this.loadUser(userId);

        if (user == null)
            throw new UserNotFoundException("User id is \"" + userId + "\" not found.");
        
        if(user.getUsername() != null && user.getUsername().equals(username)){
            return;
        }
        
        this.validUsername(username);
        
        user.setUsername(username);

        this.userDao.update(user);
    }
    
    public void updateEmail(String userId, String email) throws UserNotFoundException {
        Assert.notNull(email, "email is null");
        email = email.trim();        
        User user = this.loadUser(userId);

        if (user == null)
            throw new UserNotFoundException("User id is \"" + userId + "\" not found.");

        if(user.getEmail() != null && user.getEmail().equals(email)){
            return;
        }
        
        this.validEmail(email);
        
        user.setEmail(email);

        this.userDao.update(user);
    }
    
    public void updateMobile(String userId, String mobile) throws UserNotFoundException {
        Assert.notNull(mobile, "mobile is null");
        mobile = mobile.trim();
        User user = this.loadUser(userId);

        if (user == null)
            throw new UserNotFoundException("User id is \"" + userId + "\" not found.");
        
        if(user.getMobile() != null && user.getMobile().equals(mobile)){
            return;
        }
        
        this.validMobile(mobile);
        user.setMobile(mobile);

        this.userDao.update(user);
    }
    
    public void updateFullname(String userId, String fullname) throws UserNotFoundException {
        Assert.notNull(fullname, "fullname is null");
        fullname = fullname.trim();
        User user = this.loadUser(userId);

        if (user == null)
            throw new UserNotFoundException("User id is \"" + userId + "\" not found.");

        if(user.getFullName() != null && user.getFullName().equals(fullname)){
            return;
        }
        
        user.setFullName(fullname);

        this.userDao.update(user);
    }
    
    public void updateAvatar(String userId, String avatarFileId) throws UserNotFoundException {
        User user = this.loadUser(userId);

        if (user == null)
            throw new UserNotFoundException("User id is \"" + userId + "\" not found.");
        
        user.setAvatar(avatarFileId);

        this.userDao.update(user);
    }

    /**
     * 修改密码，不校验旧密码
     * 
     * @param userId
     *            被修改的用户ID
     * @param newPassword
     *            新密码
     * @throws UserNotFoundException
     *             用户不存在
     */
    public void updateUserPasswordWithoutCheck(String userId, String newPassword) throws UserNotFoundException {
        User user = this.loadUser(userId);

        if (user == null)
            throw new UserNotFoundException("User id is \"" + userId + "\" not found.");

        String password = newPassword.trim();
        this.validPassword(password);

        user.setPassword(this.passwordEncoder.encode(password));

        this.userDao.update(user);

    }

    private void validUsername(String username) {
        if (username == null) {
            throw new UserAuthenticationViewException("_USERNAME_IS_NULL");
        }
        if (!(username.matches(WebConfig.getUsernameFormat()))) {
            throw new UserAuthenticationViewException("_INCORRECT_USERNAME_FORMAT");
        }
        if (this.loadUserByUsername(username) != null) {
            throw new UserAuthenticationViewException("_USERNAME_ALREADY_BE_USED");
        }        
    }
    
    private void validEmail(String email) {
        if (email == null) {
            throw new UserAuthenticationViewException("_EMAIL_IS_NULL");
        }
        if (!(email.matches(WebConfig.getEmailFormat()))) {
            throw new UserAuthenticationViewException("_INCORRECT_EMAIL_FORMAT");
        }
        if (this.loadUserByEmail(email) != null) {
            throw new UserAuthenticationViewException("_EMAIL_ALREADY_BE_USED");

        }       
    }
    
    private void validMobile(String mobile) {
        if (mobile != null) {
            if (this.loadUserByMobile(mobile) != null) {
                throw new UserAuthenticationViewException("_MOBILE_ALREADY_BE_USED");

            }
        }
    }
    
    private void validPassword(String password) {
        if (password == null) {
            throw new UserAuthenticationViewException("_PASSWORD_IS_NULL");
        }        
        if (!password.matches(WebConfig.getPasswordFormat())) {
            throw new UserAuthenticationViewException("_INCORRECT_PASSWORD_FORMAT");
        }
        if (!(password.length() >= WebConfig.getMinPasswordLength()
                && password.length() <= WebConfig.getMaxPasswordLength())) {
            throw new UserAuthenticationViewException("_INCORRECT_PASSWORD_LENGTH");
        }
    }
    
    /**
     * 为用户添加用户组
     * @param userId 用户Id
     * @param groupId 待添加的用户组ID列表,如果指定的用户组ID不存在则忽略此错误ID
     * @throws UserNotFoundException 用户不存在
     */
    public void addGroup(String userId, String... groupId) throws UserNotFoundException {
        List<Group> groups = this.groupDao.load(groupId);
        
        if(groups == null || groups.isEmpty())
            return;
        
        User user = this.loadUser(userId);

        if (user == null)
            throw new UserNotFoundException("User id is \"" + userId + "\" not found.");
        
        if(user.getGroups() == null)
            user.setGroups(new HashSet<Group>());        
        user.getGroups().addAll(groups);
        
        this.userDao.update(user);        
    }
    
    /**
     * 将用户从用户组移除
     * @param userId 用户ID
     * @param groupId 待添加的用户组ID列表,如果指定的用户组ID不存在则忽略此错误ID
     * @throws UserNotFoundException 用户不存在
     */
    public void removeGroup(String userId, String... groupId) throws UserNotFoundException {
        List<Group> groups = this.groupDao.load(groupId);
        
        if(groups == null || groups.isEmpty())
            return;
        
        User user = this.loadUser(userId);

        if (user == null)
            throw new UserNotFoundException("User id is \"" + userId + "\" not found.");
        
        if(user.getGroups() == null)
            return;
        
        user.getGroups().removeAll(groups);
        
        this.userDao.update(user);   
        
    }
}
