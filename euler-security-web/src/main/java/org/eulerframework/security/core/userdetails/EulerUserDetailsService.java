package org.eulerframework.security.core.userdetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class EulerUserDetailsService implements UserDetailsService {
    private final Logger logger = LoggerFactory.getLogger(EulerUserDetailsService.class);

    private final List<EulerUserDetailsProvider> eulerUserDetailsProviders = new ArrayList<>();

    public EulerUserDetailsService(EulerUserDetailsProvider... eulerUserDetailsProvider) {
        Assert.notEmpty(eulerUserDetailsProvider, "eulerUserDetailsProvider must not be empty");
        this.eulerUserDetailsProviders.addAll(Arrays.asList(eulerUserDetailsProvider));
    }

    public EulerUserDetailsService(List<EulerUserDetailsProvider> eulerUserDetailsProviders) {
        Assert.notEmpty(eulerUserDetailsProviders, "eulerUserDetailsPreulerUserDetailsProvidersovider must not be empty");
        this.eulerUserDetailsProviders.addAll(eulerUserDetailsProviders);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EulerUserDetails userDetails = null;
        for(EulerUserDetailsProvider provider : this.eulerUserDetailsProviders) {
            if((userDetails = provider.provide(username)) != null) {
                break;
            }
        }

        if (userDetails == null) {
            throw new UsernameNotFoundException("user '" + username + "' not exists.");
        } else {
            return userDetails;
        }
    }
}
