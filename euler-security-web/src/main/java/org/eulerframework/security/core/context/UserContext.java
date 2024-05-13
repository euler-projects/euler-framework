package org.eulerframework.security.core.context;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserContext {
    UserDetails getCurrentUser();
}
