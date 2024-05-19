package org.eulerframework.security.web.context;

import org.eulerframework.security.core.context.UserContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class UsernamePasswordAuthenticationUserContext implements UserContext {
    @Override
    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }

        return null;
    }

    @Override
    public String getTenantId() {
        return "1";
    }
}
