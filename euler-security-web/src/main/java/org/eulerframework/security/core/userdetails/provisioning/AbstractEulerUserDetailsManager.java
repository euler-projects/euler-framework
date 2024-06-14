/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.security.core.userdetails.provisioning;

import org.eulerframework.security.core.EulerUser;
import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.util.UserDetailsUtils;
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
    private final SecurityContextHolderStrategy securityContextHolderStrategy;

    private AuthenticationManager authenticationManager;

    protected AbstractEulerUserDetailsManager(EulerUserService eulerUserService) {
        this(eulerUserService, SecurityContextHolder.getContextHolderStrategy());
    }

    protected AbstractEulerUserDetailsManager(EulerUserService eulerUserService, SecurityContextHolderStrategy securityContextHolderStrategy) {
        Assert.notNull(eulerUserService, "eulerUserService must not be null");
        Assert.notNull(securityContextHolderStrategy, "securityContextHolderStrategy must not be null");
        this.eulerUserService = eulerUserService;
        this.securityContextHolderStrategy = securityContextHolderStrategy;
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

        EulerUserDetails userDetails = this.loadUserByPrincipal(username);
        if (userDetails == null) {
            throw new UserDetailsNotFountException(username);
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
    public EulerUserDetails updatePassword(UserDetails userDetails, String newPassword) {
        EulerUserDetails eulerUserDetails = this.castUserDetails(userDetails);
        this.eulerUserService.updatePassword(eulerUserDetails.getUserId(), newPassword);
        EulerUser eulerUser = this.eulerUserService.loadUserById(eulerUserDetails.getUserId());
        return UserDetailsUtils.toEulerUserDetails(eulerUser);
    }

    @Override
    public void createUser(UserDetails userDetails) {
        EulerUserDetails eulerUserDetails = this.castUserDetails(userDetails);
        this.eulerUserService.createUser(this.eulerUserService.parseUserDetails(eulerUserDetails));
    }

    @Override
    public void updateUser(UserDetails userDetails) {
        EulerUserDetails eulerUserDetails = this.castUserDetails(userDetails);
        this.eulerUserService.updateUser(this.eulerUserService.parseUserDetails(eulerUserDetails));
    }

    @Override
    public void deleteUser(String username) {
        EulerUserDetails userDetails = this.loadUserByPrincipal(username);
        if (userDetails != null) {
            this.eulerUserService.deleteUser(userDetails.getUserId());
        }
    }

    @Override
    public void disableUser(String username) {
        EulerUserDetails userDetails = this.loadUserByPrincipal(username);
        if (userDetails != null) {
            this.eulerUserService.disableUser(userDetails.getUserId());
        }
    }

    protected EulerUserDetails castUserDetails(UserDetails userDetails) {
        Assert.notNull(userDetails, "userDetails must not be null");
        Assert.isInstanceOf(EulerUserDetails.class, userDetails,
                () -> "Only EulerUserDetails is supported, actually: " + userDetails.getClass().getName());
        return (EulerUserDetails) userDetails;
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
}
