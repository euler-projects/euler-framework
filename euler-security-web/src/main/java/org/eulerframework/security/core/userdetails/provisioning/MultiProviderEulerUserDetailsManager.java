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

import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiProviderEulerUserDetailsManager extends AbstractEulerUserDetailsManager {
    private final Logger logger = LoggerFactory.getLogger(MultiProviderEulerUserDetailsManager.class);

    private final List<EulerUserDetailsProvider> eulerUserDetailsProviders = new ArrayList<>();
    private final EulerUserService eulerUserService;

    public MultiProviderEulerUserDetailsManager(EulerUserService eulerUserService, EulerUserDetailsProvider... eulerUserDetailsProvider) {
        Assert.notNull(eulerUserService, "eulerUserService must not be null");
        Assert.notEmpty(eulerUserDetailsProvider, "eulerUserDetailsProvider must not be empty");
        this.eulerUserService = eulerUserService;
        this.eulerUserDetailsProviders.addAll(Arrays.asList(eulerUserDetailsProvider));
    }

    public MultiProviderEulerUserDetailsManager(EulerUserService eulerUserService, List<EulerUserDetailsProvider> eulerUserDetailsProviders) {
        Assert.notNull(eulerUserService, "eulerUserService must not be null");
        Assert.notEmpty(eulerUserDetailsProviders, "eulerUserDetailsProviders must not be empty");
        this.eulerUserService = eulerUserService;
        this.eulerUserDetailsProviders.addAll(eulerUserDetailsProviders);
    }

    @Override
    public EulerUserDetails provideUserDetails(String username) {
        EulerUserDetails userDetails = null;
        for (EulerUserDetailsProvider provider : this.eulerUserDetailsProviders) {
            if ((userDetails = provider.provide(username)) != null) {
                break;
            }
        }
        return userDetails;
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
        if(userDetails != null) {
            this.eulerUserService.deleteUser(userDetails.getUserId());
        }
    }

    @Override
    public boolean userExists(String username) {
        return this.provideUserDetails(username) != null;
    }
}
