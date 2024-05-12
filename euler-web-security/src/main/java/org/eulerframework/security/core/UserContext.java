package org.eulerframework.security.core;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserContext {
    UserDetails getCurrentUser();
}
