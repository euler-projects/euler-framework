package net.eulerframework.web.module.authentication.service;

import net.eulerframework.web.core.base.service.impl.BaseService;

import javax.annotation.Resource;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import net.eulerframework.common.util.Assert;
import net.eulerframework.web.module.authentication.dao.UserDao;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.principal.EulerUserDetails;

@Service("userDetailsService")
public class EulerUserDetailsServiceImpl extends BaseService implements EulerUserDetailsService {

    @Resource private UserDao userDao;

    @Override
    public EulerUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Assert.notNull(username, "username can not be null");
        
        User user = this.userDao.loadUserByUsername(username);
        
        if(user != null) {
            return user.toEulerUserDetails();
        }
        
        throw new UsernameNotFoundException("User '" + username + "' not found.");
    }
}
