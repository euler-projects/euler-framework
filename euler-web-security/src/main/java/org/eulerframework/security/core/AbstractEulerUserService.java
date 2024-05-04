package org.eulerframework.security.core;

import org.eulerframework.security.spring.principal.EulerUserDetails;
import org.springframework.util.Assert;

public abstract class AbstractEulerUserService implements EulerUserService {

    @Override
    public EulerUserDetails toEulerUserDetails(EulerUser eulerUser) {
        if(eulerUser == null) {
            return null;
        }

        Assert.hasText(eulerUser.getUserId(), "user id is empty");
        Assert.hasText(eulerUser.getUsername(), "username is empty");
        Assert.hasText(eulerUser.getPassword(), "password is empty");

        EulerUserDetails eulerUserDetails = new EulerUserDetails();

        eulerUserDetails.setUserId(eulerUser.getUserId());
        eulerUserDetails.setUsername(eulerUser.getUsername());
         eulerUserDetails.setPassword(eulerUser.getPassword());
        eulerUserDetails.setAccountNonLocked(eulerUser.isAccountNonLocked());
        eulerUserDetails.setAccountNonExpired(eulerUser.isAccountNonExpired());
        eulerUserDetails.setCredentialsNonExpired(eulerUser.isCredentialsNonExpired());
        eulerUserDetails.setEnabled(eulerUser.isEnabled());

        return eulerUserDetails;
    }
}
