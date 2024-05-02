package org.eulerframework.security.spring.userdetails;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.common.util.function.Wrapper;
import org.eulerframework.security.conf.SecurityConfig;
import org.eulerframework.security.core.EulerUser;
import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.spring.principal.EulerUserDetails;
import org.eulerframework.security.spring.util.EulerSecurityModelUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

public class EulerUserDetailsService implements UserDetailsService {
    private final static Cache<String, /* Use wrapper to cache null */ Wrapper<EulerUserDetails>> USER_DETAILS_CACHE =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(Duration.ofMinutes(SecurityConfig.getUserDetailsCacheLife()))
                    .build();

    private boolean userDetailsCacheEnabled = SecurityConfig.isEnableUserDetailsCache();
    private boolean enableEmailSignIn = SecurityConfig.isEnableEmailSignIn();
    private boolean enableMobileSignIn = SecurityConfig.isEnableMobileSignIn();

    private final EulerUserService eulerUserService;

    public EulerUserDetailsService(EulerUserService eulerUserService) {
        this.eulerUserService = eulerUserService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EulerUserDetails userDetails;
        if (this.userDetailsCacheEnabled) {
            try {
                userDetails = USER_DETAILS_CACHE.get(username, () -> new Wrapper<>(this.loadUserDetails(username))).get();
            } catch (ExecutionException e) {
                throw ExceptionUtils.asRuntimeException(e);
            }
        } else {
            userDetails = this.loadUserDetails(username);
        }

        if (userDetails == null) {
            throw new UsernameNotFoundException("user '" + username + "' not exists.");
        } else {
            return userDetails;
        }
    }

    private EulerUserDetails loadUserDetails(String username) {
        EulerUser eulerUser = this.eulerUserService.loadUserByUsername(username);

        if (eulerUser == null && this.enableEmailSignIn) {
            eulerUser = this.eulerUserService.loadUserByEmail(username);
        }

        if (eulerUser == null && this.enableMobileSignIn) {
            eulerUser = this.eulerUserService.loadUserByMobile(username);
        }

        return EulerSecurityModelUtils.toEulerUserDetails(eulerUser);
    }
}
