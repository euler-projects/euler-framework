package org.eulerframework.security.core.userdetails.provisioning;

import org.eulerframework.security.core.userdetails.DefaultEulerUserDetails;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;

public abstract class AbstractEulerUserDetailsManager implements EulerUserDetailsManager {
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails = this.provideUserDetails(username);

        if (userDetails == null || CollectionUtils.isEmpty(userDetails.getAuthorities())) {
            throw new UsernameNotFoundException("User '" + username + "' not found.");
        } else {
            return userDetails;
        }
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        if (passwordEncoder == null) {
            throw new UnsupportedOperationException("No password encoder available");
        }
        UserDetails currentUser = new DefaultEulerUserDetails(null, null, null, null); // TODO: get current user details;

        if (!this.passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new BadCredentialsException("Incorrect old password");
        }

        this.updatePassword(currentUser, newPassword);
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
