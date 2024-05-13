package org.eulerframework.security.core.userdetails;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class DefaultEulerUserDetails extends User implements EulerUserDetails {
    private final String userId;

    public DefaultEulerUserDetails(String userId, String username, String password,
                                   Collection<? extends GrantedAuthority> authorities) {
        this(userId, username, password, true, true, true, true, authorities);
    }

    public DefaultEulerUserDetails(String userId, String username, String password, boolean enabled, boolean accountNonExpired,
                                   boolean credentialsNonExpired, boolean accountNonLocked,
                                   Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
