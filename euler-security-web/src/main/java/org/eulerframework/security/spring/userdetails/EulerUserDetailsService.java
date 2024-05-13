package org.eulerframework.security.spring.userdetails;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.eulerframework.security.core.EulerUser;
import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.spring.principal.EulerUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Optional;

public class EulerUserDetailsService implements UserDetailsService {
    private final Logger logger = LoggerFactory.getLogger(EulerUserDetailsService.class);

    private final LoadingCache<String, /* Use optional to cache null */ Optional<EulerUserDetails>> userDetailsCache;
    private final boolean enableEmailSignIn;
    private final boolean enableMobileSignIn;
    private final EulerUserService eulerUserService;

    public EulerUserDetailsService(EulerUserService eulerUserService, boolean enableEmailSignIn, boolean enableMobileSignIn, long userDetailsCacheExpireMillis) {
        this.eulerUserService = eulerUserService;
        this.enableEmailSignIn = enableEmailSignIn;
        this.enableMobileSignIn = enableMobileSignIn;
        if (userDetailsCacheExpireMillis > 0) {
            this.userDetailsCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(Duration.ofMinutes(userDetailsCacheExpireMillis))
                    .build(new CacheLoader<>() {
                        @Override
                        @Nonnull
                        public Optional<EulerUserDetails> load(@Nonnull String username) {
                            EulerUserDetailsService.this.logger.debug("Loading user '{}' from database.", username);
                            EulerUserDetails userDetails = EulerUserDetailsService.this.loadUserDetails(username);

                            if (userDetails == null) {
                                EulerUserDetailsService.this.logger.debug("User '{}' not found.", username);
                            } else {
                                EulerUserDetailsService.this.logger.debug("User '{}({})' loaded.", username, userDetails.getUserId());
                            }

                            return Optional.ofNullable(userDetails);
                        }
                    });
        } else {
            this.userDetailsCache = null;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EulerUserDetails userDetails = this.loadUserDetails(username);
//        if (this.userDetailsCache != null) {
//            Optional<EulerUserDetails> eulerUserDetails = this.userDetailsCache.getUnchecked(username);
//            userDetails = Objects.requireNonNull(eulerUserDetails).orElse(null);
//        } else {
//            userDetails = this.loadUserDetails(username);
//        }

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

        return this.eulerUserService.toEulerUserDetails(eulerUser);
    }
}
