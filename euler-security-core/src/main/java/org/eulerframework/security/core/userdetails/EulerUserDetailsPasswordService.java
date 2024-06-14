package org.eulerframework.security.core.userdetails;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;

public interface EulerUserDetailsPasswordService extends UserDetailsPasswordService {

    @Override
    EulerUserDetails updatePassword(UserDetails user, String newPassword);

}
