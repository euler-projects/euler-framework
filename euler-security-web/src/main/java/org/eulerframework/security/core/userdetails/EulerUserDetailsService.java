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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EulerUserDetailsService implements UserDetailsService {
    private final Logger logger = LoggerFactory.getLogger(EulerUserDetailsService.class);

    private final List<EulerUserDetailsProvider> eulerUserDetailsProviders = new ArrayList<>();

    public EulerUserDetailsService(EulerUserDetailsProvider... eulerUserDetailsProvider) {
        Assert.notEmpty(eulerUserDetailsProvider, "eulerUserDetailsProvider must not be empty");
        this.eulerUserDetailsProviders.addAll(Arrays.asList(eulerUserDetailsProvider));
    }

    public EulerUserDetailsService(List<EulerUserDetailsProvider> eulerUserDetailsProviders) {
        Assert.notEmpty(eulerUserDetailsProviders, "eulerUserDetailsProviders must not be empty");
        this.eulerUserDetailsProviders.addAll(eulerUserDetailsProviders);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EulerUserDetails userDetails = null;
        for(EulerUserDetailsProvider provider : this.eulerUserDetailsProviders) {
            if((userDetails = provider.provide(username)) != null) {
                break;
            }
        }

        if (userDetails == null) {
            throw new UsernameNotFoundException("user '" + username + "' not exists.");
        } else {
            return userDetails;
        }
    }
}
