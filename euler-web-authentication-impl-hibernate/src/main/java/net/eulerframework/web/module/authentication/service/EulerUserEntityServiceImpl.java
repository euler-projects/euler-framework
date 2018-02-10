package net.eulerframework.web.module.authentication.service;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.dao.UserDao;
import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;

@Service
public class EulerUserEntityServiceImpl extends BaseService implements EulerUserEntityService {

    @Resource private UserDao userDao;

    @Override
    public User loadUserByUserId(String userId) throws UserNotFoundException {
        return this.userDao.load(userId);
    }

    @Override
    public User loadUserByUsername(String username) throws UserNotFoundException {
        User result = this.userDao.loadUserByUsername(username);
        
        if(result == null) {
            throw new UserNotFoundException();
        }
        
        return result;
    }

    @Override
    public User loadUserByEmail(String email) throws UserNotFoundException {
        User result = this.userDao.loadUserByEmail(email);
        
        if(result == null) {
            throw new UserNotFoundException();
        }
        
        return result;
    }

    @Override
    public User loadUserByMobile(String mobile) throws UserNotFoundException {
        User result = this.userDao.loadUserByMobile(mobile);
        
        if(result == null) {
            throw new UserNotFoundException();
        }
        
        return result;
    }

    @Override
    public User createUser(EulerUserEntity eulerUserEntity) {
        Assert.isTrue(User.class.isAssignableFrom(eulerUserEntity.getClass()), 
                "eulerUserEntity must be an instance of net.eulerframework.web.module.authentication.entity.User");
        User user = (User)eulerUserEntity;
        user.setRegistTime(new Date());
        this.userDao.save(user);
        return user;
    }

    @Override
    public void updateUser(EulerUserEntity eulerUserEntity) {
        Assert.isTrue(User.class.isAssignableFrom(eulerUserEntity.getClass()), 
                "eulerUserEntity must be an instance of net.eulerframework.web.module.authentication.entity.User");
        User user = (User)eulerUserEntity;
        this.userDao.update(user);
    }
}
