package org.eulerframework.security.core;

import org.springframework.security.core.GrantedAuthority;

public interface EulerAuthority extends GrantedAuthority {
    String getName();
    String getDescription();
}
