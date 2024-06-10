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

import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.exception.UserDetailsNotFountException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;

public class DelegatingEulerUserDetailsManager implements EulerUserDetailsManager {
    /**
     * An ordered {@link EulerUserDetailsManager} map which key is each manager's support {@link UserDetails} type.
     * The map's order will affect
     * the loading order of method {@link DelegatingEulerUserDetailsManager#provideUserDetails(String)}
     * and the checking order of method {@link DelegatingEulerUserDetailsManager#userExists(String)}.
     */
    private final LinkedHashMap<Class<? extends UserDetails>, EulerUserDetailsManager> eulerUserDetailsManagers = new LinkedHashMap<>();
    private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
            .getContextHolderStrategy();

    public DelegatingEulerUserDetailsManager(LinkedHashMap<Class<? extends UserDetails>, EulerUserDetailsManager> eulerUserDetailsManagers) {
        Assert.notEmpty(eulerUserDetailsManagers, "eulerUserDetailsManagers must not be empty");
        this.eulerUserDetailsManagers.putAll(eulerUserDetailsManagers);
    }

    @Override
    public EulerUserDetails provideUserDetails(String username) {
        EulerUserDetails userDetails = null;
        for (EulerUserDetailsManager manager : eulerUserDetailsManagers.values()) {
            if ((userDetails = manager.provideUserDetails(username)) != null) {
                break;
            }
        }
        return userDetails;
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        return this.getEulerUserDetailsManager(user.getClass()).updatePassword(user, newPassword);
    }

    @Override
    public void createUser(UserDetails user) {
        Assert.isTrue(!this.userExists(user.getUsername()), "User already exists");
        this.getEulerUserDetailsManager(user.getClass()).createUser(user);
    }

    @Override
    public void updateUser(UserDetails user) {
        this.getEulerUserDetailsManager(user.getClass()).updateUser(user);
    }

    @Override
    public void deleteUser(String username) {
        UserDetails userDetails = this.provideUserDetails(username);
        if (userDetails != null) {
            this.getEulerUserDetailsManager(userDetails.getClass()).deleteUser(username);
        }
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
        if (userDetails == null) {
            throw new UserDetailsNotFountException("User '" + username + "' not found");
        }

        this.getEulerUserDetailsManager(userDetails.getClass()).changePassword(oldPassword, newPassword);
    }

    @Override
    public boolean userExists(String username) {
        for (EulerUserDetailsManager manager : eulerUserDetailsManagers.values()) {
            if (manager.userExists(username)) {
                return true;
            }
        }
        return false;
    }

    public void addEulerUserDetailsManager(Class<? extends UserDetails> type, EulerUserDetailsManager eulerUserDetailsManager) {
        Assert.notNull(type, "type must not be null");
        Assert.notNull(eulerUserDetailsManager, "eulerUserDetailsManager must not be null");
        Assert.isTrue(!this.eulerUserDetailsManagers.containsKey(type),
                () -> "EulerUserDetailsManager for user details type '" + type + "' already exists");

        this.eulerUserDetailsManagers.put(type, eulerUserDetailsManager);
    }

    private EulerUserDetailsManager getEulerUserDetailsManager(Class<? extends UserDetails> clazz) {
        EulerUserDetailsManager manager = eulerUserDetailsManagers.get(clazz);
        if (manager == null) {
            throw new IllegalArgumentException("EulerUserDetailsManager for user details type is '" + clazz + "' not found");
        }
        return manager;
    }

    public void setSecurityContextHolderStrategy(SecurityContextHolderStrategy securityContextHolderStrategy) {
        this.securityContextHolderStrategy = securityContextHolderStrategy;
    }
}
