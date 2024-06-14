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
package org.eulerframework.security.core.userdetails;

import org.eulerframework.security.core.EulerUser;
import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.core.userdetails.provisioning.EulerUserDetailsManager;
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

public class DefaultEulerUserDetailsService implements EulerUserDetailsService, EulerUserDetailsPasswordService {
    private final Logger logger = LoggerFactory.getLogger(DefaultEulerUserDetailsService.class);

    private final EulerUserService eulerUserService;

    private AuthenticationManager authenticationManager;

    protected DefaultEulerUserDetailsService(EulerUserService eulerUserService) {
        Assert.notNull(eulerUserService, "eulerUserService must not be null");
        this.eulerUserService = eulerUserService;
    }

    @Override
    public EulerUserDetails loadUserByPrincipal(String principal) {
        EulerUser eulerUser = this.eulerUserService.loadUserByUsername(principal);
        return UserDetailsUtils.toEulerUserDetails(eulerUser);
    }

    @Override
    public EulerUserDetails updatePassword(UserDetails userDetails, String newPassword) {
        EulerUserDetails eulerUserDetails = this.castUserDetails(userDetails);
        this.eulerUserService.updatePassword(eulerUserDetails.getUserId(), newPassword);
        EulerUser eulerUser = this.eulerUserService.loadUserById(eulerUserDetails.getUserId());
        return UserDetailsUtils.toEulerUserDetails(eulerUser);
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

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}
