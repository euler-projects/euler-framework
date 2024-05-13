package org.eulerframework.security.core.userdetails;

public interface EulerUserDetailsProvider {
    EulerUserDetails provide(String principal);
}
