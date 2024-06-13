package org.eulerframework.security.core.userdetails;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface EulerUserDetailsService extends UserDetailsService {
    @Override
    EulerUserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
