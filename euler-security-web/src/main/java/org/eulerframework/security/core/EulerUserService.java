package org.eulerframework.security.core;

public interface EulerUserService {
    void signUp(String username, String email, String mobile, String password);
}
