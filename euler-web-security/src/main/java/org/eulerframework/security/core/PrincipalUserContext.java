package org.eulerframework.security.core;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public abstract class PrincipalUserContext implements UserContext {
    protected abstract UserDetails getUserDetails(Authentication authentication);

    @Override
    public UserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return this.getUserDetails(authentication);
    }
}
