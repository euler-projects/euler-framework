package net.eulerframework.web.module.authentication.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import net.eulerframework.common.util.Assert;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.dao.IGroupDao;
import net.eulerframework.web.module.authentication.dao.IUserDao;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.service.IUserService;

@Service
public class UserService extends BaseService implements IUserService {

    @Resource private IUserDao userDao;
    @Resource private IGroupDao groupDao;

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

    @Override
    public void updateUser(User user) throws UserNotFoundException {
        Assert.isNotNull(user.getId(), "userid is null");
        
        User existedUser = this.userDao.load(user.getId());
        
        if(existedUser == null)
            throw new UserNotFoundException("User id is \"" + user.getId() + "\" not found.");
        
        user.setPassword(existedUser.getPassword());
        
        this.userDao.update(user);
        
    }
}
