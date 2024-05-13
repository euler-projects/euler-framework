package org.eulerframework.security.core;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DelegatingPrincipalUserContext extends PrincipalUserContext {
    private final List<PrincipalUserContext> userContexts = new ArrayList<>();

    public DelegatingPrincipalUserContext(PrincipalUserContext... userContext) {
        this.userContexts.addAll(Arrays.asList(userContext));
    }

    @Override
    protected UserDetails getUserDetails(Authentication authentication) {
        UserDetails userDetails = null;
        for (PrincipalUserContext userContext : this.userContexts) {
            if ((userDetails = userContext.getUserDetails(authentication)) != null) {
                return userDetails;
            }
        }
        return userDetails;
    }
}
