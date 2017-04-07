package net.eulerframework.web.module.authentication.service;

import javax.annotation.Resource;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import net.eulerframework.common.util.Assert;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.service.UserService;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource private UserService userSerivce;
    
    private boolean enableEmailSignin = WebConfig.isEnableMobileSignin();
    private boolean enableMobileSignin = WebConfig.isEnableMobileSignin();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Assert.notNull(username, "username is null");
        
        User user = this.userSerivce.loadUserByUsername(username);
        if(user == null && enableEmailSignin) {
            user = this.userSerivce.loadUserByEmail(username);
        }
        if(user == null && enableMobileSignin) {
            user = this.userSerivce.loadUserByMobile(username);
        }
        if(user == null) {
            throw new UsernameNotFoundException("User \"" + username + "\" not found.");
        }
        return user;
    }
}
