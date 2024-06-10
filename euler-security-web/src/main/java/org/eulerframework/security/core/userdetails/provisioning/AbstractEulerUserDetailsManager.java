package org.eulerframework.security.core.userdetails.provisioning;

import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.exception.UserDetailsNotFountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

public abstract class AbstractEulerUserDetailsManager implements EulerUserDetailsManager {
    private final Logger logger = LoggerFactory.getLogger(AbstractEulerUserDetailsManager.class);

    private final EulerUserService eulerUserService;

    private AuthenticationManager authenticationManager;
    private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
            .getContextHolderStrategy();

    protected AbstractEulerUserDetailsManager(EulerUserService eulerUserService) {
        Assert.notNull(eulerUserService, "eulerUserService must not be null");
        this.eulerUserService = eulerUserService;
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Authentication currentUser = this.securityContextHolderStrategy.getContext().getAuthentication();
        if (currentUser == null) {
            // This would indicate bad coding somewhere
            throw new AccessDeniedException(
                    "Can't change password as no Authentication object found in context " + "for current user.");
        }
        String username = currentUser.getName();

        EulerUserDetails userDetails = this.provideUserDetails(username);
        if(userDetails == null) {
            throw new UserDetailsNotFountException("User '" + username + "' not found");
        }

        this.logger.debug("Changing password for user '{}'", username);
        // If an authentication manager has been set, re-authenticate the user with the
        // supplied password.
        if (this.authenticationManager != null) {
            this.logger.debug("Re-authenticating user '{}' for password change request.", username);
            try {
                this.authenticationManager
                        .authenticate(UsernamePasswordAuthenticationToken.unauthenticated(username, oldPassword));
            } catch (AuthenticationException e) {
                this.logger.debug("Failed to authenticate user '{}' for password change request.", username, e);
                throw new IllegalArgumentException("Incorrect old password");
            }
        } else {
            this.logger.debug("No authentication manager set. Password won't be re-checked.");
        }
        this.logger.debug("Changing password for user '" + username + "'");
        this.updatePassword(userDetails, newPassword);
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        Assert.isAssignable(EulerUserDetails.class, user.getClass(), () -> "Only EulerUserDetails is supported, actually: " + user.getClass().getName());
        EulerUserDetails userDetails = (EulerUserDetails) user;
        return this.eulerUserService.updatePassword(userDetails.getUserId(), newPassword);
    }

    @Override
    public void createUser(UserDetails user) {
        Assert.isAssignable(EulerUserDetails.class, user.getClass(), () -> "Only EulerUserDetails is supported, actually: " + user.getClass().getName());
        EulerUserDetails userDetails = (EulerUserDetails) user;
        this.eulerUserService.createUser(userDetails);
    }

    @Override
    public void updateUser(UserDetails user) {
        Assert.isAssignable(EulerUserDetails.class, user.getClass(), () -> "Only EulerUserDetails is supported, actually: " + user.getClass().getName());
        EulerUserDetails userDetails = (EulerUserDetails) user;
        this.eulerUserService.updateUser(userDetails);
    }

    @Override
    public void deleteUser(String username) {
        EulerUserDetails userDetails = this.provideUserDetails(username);
        if (userDetails != null) {
            this.eulerUserService.deleteUser(userDetails.getUserId());
        }
    }

    protected EulerUserService getEulerUserService() {
        return eulerUserService;
    }

    protected AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    protected SecurityContextHolderStrategy getSecurityContextHolderStrategy() {
        return securityContextHolderStrategy;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setSecurityContextHolderStrategy(SecurityContextHolderStrategy securityContextHolderStrategy) {
        this.securityContextHolderStrategy = securityContextHolderStrategy;
    }
}
