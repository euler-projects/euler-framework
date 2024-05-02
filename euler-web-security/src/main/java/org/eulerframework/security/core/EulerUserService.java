package org.eulerframework.security.core;

public interface EulerUserService {
    EulerUser loadUserByUsername(String username);
    EulerUser loadUserByEmail(String email);
    EulerUser loadUserByMobile(String mobile);
}
