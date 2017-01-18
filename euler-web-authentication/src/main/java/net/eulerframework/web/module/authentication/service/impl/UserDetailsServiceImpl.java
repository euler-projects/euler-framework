package net.eulerframework.web.module.authentication.service.impl;

import javax.annotation.Resource;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import net.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import net.eulerframework.cache.inMemoryCache.ObjectCachePool;
import net.eulerframework.common.util.Assert;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.service.IUserService;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource private IUserService userSerivce;
    
    private boolean enableEmailSignin = WebConfig.isEnableMobileSignin();
    private boolean enableMobileSignin = WebConfig.isEnableMobileSignin();
    private boolean enableUserCache = WebConfig.isEnableUserCache();
    
    private static final DefaultObjectCache<String, User> USER_CACHE = ObjectCachePool.generateDefaultObjectCache(WebConfig.getUserAuthenticationCacheLife());

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Assert.isNotNull(username, "username is null");
        
        User user = null;
        if(enableUserCache) {
            user = USER_CACHE.get(username);
        }
        if(user == null) {
            user = this.userSerivce.loadUserByUsername(username);
            if(user == null && enableEmailSignin) {
                user = this.userSerivce.loadUserByEmail(username);
            }
            if(user == null && enableMobileSignin) {
                user = this.userSerivce.loadUserByMobile(username);
            }
            if(user == null) {
                throw new UsernameNotFoundException("User \"" + username + "\" not found.");
            }
            if(enableUserCache) {
                USER_CACHE.put(username, user);
            }
        }
        return user;
    }
}
