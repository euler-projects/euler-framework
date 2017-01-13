package net.eulerframework.web.module.authentication.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import net.eulerframework.common.util.Assert;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.core.exception.BadRequestException;
import net.eulerframework.web.module.authentication.Lang;
import net.eulerframework.web.module.authentication.dao.IGroupDao;
import net.eulerframework.web.module.authentication.dao.IUserDao;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.IncorrectPasswordException;
import net.eulerframework.web.module.authentication.exception.IncorrectPasswordFormatException;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.service.IUserService;

@Service
public class UserService extends BaseService implements IUserService {

    @Resource private IUserDao userDao;
    @Resource private IGroupDao groupDao;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public User loadUserByUsername(String username) {
        Assert.isNotNull(username, "username is null");
        User user = this.userDao.findUserByName(username);
        return user;
    }

    @Override
    public User loadUserByEmail(String email) {
        Assert.isNotNull(email, "email is null");
        User user = this.userDao.findUserByEmail(email);        
        return user;
    }

    @Override
    public User loadUserByMobile(String mobile) {
        Assert.isNotNull(mobile, "mobile is null");
        User user =  this.userDao.findUserByEmail(mobile);        
        return user;
    }

    @Override
    public List<User> loadUserByNameOrCodeFuzzy(String nameOrCode) {
        Assert.isNotNull(nameOrCode, "nameOrCode is null");
        return this.userDao.findUserByNameOrCode(nameOrCode);
    }

    @Override
    public User loadUser(String userId) {
        Assert.isNotNull(userId, "userId is null");
        return this.userDao.load(userId);
    }

    @Override
    public String save(User user) {
        Assert.isNotNull(user, "user is null");
        return (String) this.userDao.save(user);
    }

    @Override
    public PageResponse<User> findUserByPage(PageQueryRequest pageQueryRequest) {
        return this.userDao.findEntityInPage(pageQueryRequest);
    }

    /**
     * 更新处密码外的用户信息,注意用户权限，用户组等的处理，如不指定这些字段，原有字段会被删掉。
     * @param user 更新用户实体，不需要指定password字段，指定也会无效
     * @throws UserNotFoundException 被更新的用户不存在
     */
    @Override
    public void updateUser(User user) throws UserNotFoundException {
        Assert.isNotNull(user.getId(), "userid is null");
        
        User existedUser = this.userDao.load(user.getId());
        
        if(existedUser == null)
            throw new UserNotFoundException("User id is \"" + user.getId() + "\" not found.");
        
        user.setPassword(existedUser.getPassword());
        user.setSignUpTime(existedUser.getSignUpTime());
        
        this.userDao.update(user);
        
    }

    @Override
    public void updateUserPassword(String userId, String oldPassword, String newPassword) throws UserNotFoundException, IncorrectPasswordException, IncorrectPasswordFormatException {
        User user = this.loadUser(userId);
        
        if(user == null)
            throw new UserNotFoundException();

        if (!this.passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IncorrectPasswordException(Lang.PASSWD.INCORRECT_PASSWD.toString());
        }

        String password;
        try {
            password = newPassword.trim();
            Assert.isTrue(password.matches(WebConfig.getPasswordFormat()), BadRequestException.class,
                    Lang.PASSWD.INCORRECT_PASSWD_FORMAT.toString());
            Assert.isTrue(password.length() >= WebConfig.getMinPasswordLength() && password.length() <= 20,
                    BadRequestException.class, Lang.PASSWD.INCORRECT_PASSWD_LENGTH.toString());
        } catch (BadRequestException e) {
            throw new IncorrectPasswordFormatException(e.getMessage(), e);
        }
        
        user.setPassword(this.passwordEncoder.encode(password));
        
        this.userDao.update(user);
    }
}
