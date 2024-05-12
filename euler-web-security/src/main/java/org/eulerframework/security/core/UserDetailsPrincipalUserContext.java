package org.eulerframework.security.core;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsPrincipalUserContext extends PrincipalUserContext {
    @Override
    protected UserDetails getUserDetails(Authentication authentication) {
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            return (UserDetails) authentication.getPrincipal();
        }

        return null;
    }
}
