package org.eulerframework.security.core;

import org.eulerframework.security.spring.principal.EulerUserDetails;

public interface EulerUserService {
    EulerUser loadUserByUsername(String username);
    EulerUser loadUserByEmail(String email);
    EulerUser loadUserByMobile(String mobile);
    EulerUserDetails toEulerUserDetails(EulerUser eulerUser);
}
