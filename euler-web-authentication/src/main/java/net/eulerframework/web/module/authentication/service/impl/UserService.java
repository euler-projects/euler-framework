package net.eulerframework.web.module.authentication.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import net.eulerframework.cache.DefaultObjectCache;
import net.eulerframework.cache.ObjectCachePool;
import net.eulerframework.common.util.Assert;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.module.authentication.dao.IGroupDao;
import net.eulerframework.web.module.authentication.dao.IUserDao;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.service.IUserService;

@Service
public class UserService implements IUserService {

    @Resource private IUserDao userDao;
    @Resource private IGroupDao groupDao;
    
    private boolean enableEmailSignin = false;
    private boolean enableMobileSignin = false;
    private boolean enableUserCache = false;
    
    private DefaultObjectCache<String, User> userCache;
    
    public UserService() {
        this.enableEmailSignin = WebConfig.isEnableEmailSignin();
        this.enableMobileSignin = WebConfig.isEnableMobileSignin();
        this.enableUserCache = WebConfig.isEnableUserCache();
        
        if(enableUserCache) {
            userCache = ObjectCachePool.generateDefaultObjectCache(WebConfig.getUserAuthenticationCacheLife());           
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Assert.isNotNull(username, "username is null");
        
        User user = null;
        if(enableUserCache) {
            user = this.userCache.get(username);
        }
        if(user == null) {
            user = this.userDao.findUserByName(username);
            if(user == null && enableEmailSignin) {
                user = this.userDao.findUserByEmail(username);
            }
            if(user == null && enableMobileSignin) {
                user = this.userDao.findUserByMobile(username);
            }
            if(user == null) {
                throw new UsernameNotFoundException("User \"" + username + "\" not found.");
            }
            if(enableUserCache) {
                this.userCache.put(username, user);
            }
        }
        return user;
    }

    @Override
    public User loadUserByEmail(String email) throws UsernameNotFoundException {
        Assert.isNotNull(email, "email is null");
        User user = this.userDao.findUserByEmail(email);
        
        if(user == null) {
            throw new UsernameNotFoundException("User email is \"" + email + "\" not found.");
        }
        
        return user;
    }

    @Override
    public User loadUserByMobile(String mobile) throws UsernameNotFoundException {
        Assert.isNotNull(mobile, "mobile is null");
        User user =  this.userDao.findUserByEmail(mobile);
        
        if(user == null) {
            throw new UsernameNotFoundException("User mobile is \"" + mobile + "\" not found.");
        }
        
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
}
