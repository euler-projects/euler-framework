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
import org.eulerframework.security.core.context.UserContext;
import org.eulerframework.security.core.context.UserContextHolder;
import org.eulerframework.security.core.userdetails.DefaultEulerUserDetailsService;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

public class DefaultEulerUserDetailsManager extends DefaultEulerUserDetailsService implements EulerUserDetailsManager {
    private final Logger logger = LoggerFactory.getLogger(DefaultEulerUserDetailsManager.class);

    private final UserContext userContext;

    protected DefaultEulerUserDetailsManager(EulerUserService eulerUserService) {
        this(eulerUserService, UserContextHolder.getUserContext());
    }

    protected DefaultEulerUserDetailsManager(EulerUserService eulerUserService, UserContext userContext) {
        super(eulerUserService);
        Assert.notNull(userContext, "userContext must not be null");
        this.userContext = userContext;
    }

    @Override
    public void createUser(UserDetails userDetails) {
        EulerUserDetails eulerUserDetails = this.castUserDetails(userDetails);
        EulerUser eulerUser = this.getEulerUserService().createUser(eulerUserDetails);
        if (eulerUserDetails.getUserId() == null) {
            eulerUserDetails.setUserId(eulerUser.getUserId());
        }
    }

    @Override
    public void updateUser(UserDetails userDetails) {
        EulerUserDetails eulerUserDetails = this.castUserDetails(userDetails);
        EulerUser eulerUser = this.getEulerUserService().loadUserById(eulerUserDetails.getUserId());
        eulerUser.reloadUserDetails(eulerUserDetails);
        this.getEulerUserService().updateUser(eulerUser);
    }

    @Override
    public void deleteUser(String username) {
        EulerUserDetails userDetails = this.loadUserByPrincipal(username);
        if (userDetails != null) {
            this.getEulerUserService().deleteUser(userDetails.getUserId());
        }
    }

    @Override
    public void disableUser(String username) {
        EulerUserDetails userDetails = this.loadUserByPrincipal(username);
        if (userDetails != null) {
            this.getEulerUserService().disableUser(userDetails.getUserId());
        }
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        EulerUserDetails userDetails = this.userContext.getUserDetails();

        if (userDetails == null) {
            throw new AccessDeniedException(
                    "Can't change password as no user details found in context for current user.");
        }

        String username = userDetails.getUsername();
        String userId = userDetails.getUserId();
        this.logger.debug("Changing password for user '{}'", userId);
        // If an authentication manager has been set, re-authenticate the user with the
        // supplied password.
        if (this.getAuthenticationManager() != null) {
            this.logger.debug("Re-authenticating user '{}' for password change request.", userId);
            try {
                this.getAuthenticationManager()
                        .authenticate(UsernamePasswordAuthenticationToken.unauthenticated(username, oldPassword));
            } catch (AuthenticationException e) {
                this.logger.debug("Failed to authenticate user '{}' for password change request.", userId, e);
                throw new IllegalArgumentException("Incorrect old password");
            }
        } else {
            this.logger.debug("No authentication manager set. Password won't be re-checked.");
        }
        this.logger.debug("Changing password for user '" + userId + "'");
        this.updatePassword(userDetails, newPassword);
    }

    @Override
    public boolean userExists(String username) {
        return this.loadUserByPrincipal(username) != null;
    }

    protected UserContext getUserContext() {
        return this.userContext;
    }
}
