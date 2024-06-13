package org.eulerframework.security.oauth2.server.authorization.userdetails.provisioning;

import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.core.userdetails.EulerUserDetailsService;
import org.springframework.security.oauth2.core.userdetails.provisioning.OAuth2TokenUserDetailsProvider;

public class AuthorizationServerUserDetailsProvider implements OAuth2TokenUserDetailsProvider {
    private final EulerUserDetailsService userDetailsService;

    public AuthorizationServerUserDetailsProvider(EulerUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public EulerUserDetails provide(String principal) {
        return this.userDetailsService.loadUserByUsername(principal);
    }
}
