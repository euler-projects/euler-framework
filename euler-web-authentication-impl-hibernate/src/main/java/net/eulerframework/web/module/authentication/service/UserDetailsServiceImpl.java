package net.eulerframework.web.module.authentication.service;

import net.eulerframework.web.core.base.service.impl.BaseService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import net.eulerframework.common.util.Assert;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.principal.EulerUserDetails;

@Service("userDetailsService")
public class UserDetailsServiceImpl extends BaseService implements EulerUserDetailsService {

    @Override
    public EulerUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Assert.notNull(username, "username can not be null");
        
        if("admin".equalsIgnoreCase(username)) {
            this.logger.info("admin user!");
            return new User().toEulerUserDetails();
        }
        
        throw new UsernameNotFoundException("User '" + username + "' not found.");
    }
}
