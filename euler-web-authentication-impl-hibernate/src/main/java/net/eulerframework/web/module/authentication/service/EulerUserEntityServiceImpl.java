package net.eulerframework.web.module.authentication.service;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.repository.UserRepository;

@Service
public class EulerUserEntityServiceImpl extends BaseService implements EulerUserEntityService {

    @Resource
    private UserRepository userRepository;

    @Override
    public User loadUserByUserId(String userId) throws UserNotFoundException {
        return this.userRepository.findUserById(userId);
    }

    @Override
    public User loadUserByUsername(String username) throws UserNotFoundException {
        User result = this.userRepository.findUserByUsernameIgnoreCase(username);
        
        if(result == null) {
            throw new UserNotFoundException();
        }
        
        return result;
    }

    @Override
    public User loadUserByEmail(String email) throws UserNotFoundException {
        User result = this.userRepository.findUserByEmailIgnoreCase(email);
        
        if(result == null) {
            throw new UserNotFoundException();
        }
        
        return result;
    }

    @Override
    public User loadUserByMobile(String mobile) throws UserNotFoundException {
        User result = this.userRepository.findUserByMobileIgnoreCase(mobile);
        
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
        this.userRepository.save(user);
        return user;
    }

    @Override
    public void updateUser(EulerUserEntity eulerUserEntity) {
        Assert.isTrue(User.class.isAssignableFrom(eulerUserEntity.getClass()), 
                "eulerUserEntity must be an instance of net.eulerframework.web.module.authentication.entity.User");
        User user = (User)eulerUserEntity;
        this.userRepository.save(user);
    }
}
