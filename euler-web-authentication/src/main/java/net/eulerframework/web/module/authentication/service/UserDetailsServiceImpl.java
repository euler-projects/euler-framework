package net.eulerframework.web.module.authentication.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import net.eulerframework.common.util.Assert;
import net.eulerframework.web.module.authentication.entity.EulerUserDetails;
import net.eulerframework.web.module.authentication.entity.TestUserEntity;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements EulerUserDetailsService {

    @Override
    public EulerUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Assert.notNull(username, "username is null");
        
        if("admin".equalsIgnoreCase(username)) {
            return new EulerUserDetails(new TestUserEntity());
        }
        
        throw new UsernameNotFoundException("TestUserEntity \"" + username + "\" not found.");
    }
}
