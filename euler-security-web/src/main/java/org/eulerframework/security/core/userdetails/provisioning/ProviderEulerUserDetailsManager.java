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
import org.eulerframework.security.core.context.UserContext;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProviderEulerUserDetailsManager extends DefaultEulerUserDetailsManager {
    private final Logger logger = LoggerFactory.getLogger(ProviderEulerUserDetailsManager.class);

    private final List<EulerUserDetailsProvider> eulerUserDetailsProviders = new ArrayList<>();

    public ProviderEulerUserDetailsManager(EulerUserService eulerUserService, EulerUserDetailsProvider... eulerUserDetailsProvider) {
        this(eulerUserService, Arrays.asList(eulerUserDetailsProvider));
    }

    public ProviderEulerUserDetailsManager(EulerUserService eulerUserService, UserContext userContext, EulerUserDetailsProvider... eulerUserDetailsProvider) {
        this(eulerUserService, userContext, Arrays.asList(eulerUserDetailsProvider));
    }

    public ProviderEulerUserDetailsManager(EulerUserService eulerUserService, List<? extends EulerUserDetailsProvider> eulerUserDetailsProviders) {
        super(eulerUserService);
        Assert.notEmpty(eulerUserDetailsProviders, "eulerUserDetailsProviders must not be empty");
        this.eulerUserDetailsProviders.addAll(eulerUserDetailsProviders);
    }

    public ProviderEulerUserDetailsManager(EulerUserService eulerUserService, UserContext userContext, List<? extends EulerUserDetailsProvider> eulerUserDetailsProviders) {
        super(eulerUserService, userContext);
        Assert.notEmpty(eulerUserDetailsProviders, "eulerUserDetailsProviders must not be empty");
        this.eulerUserDetailsProviders.addAll(eulerUserDetailsProviders);
    }

    @Override
    public EulerUserDetails loadUserByPrincipal(String principal) {
        EulerUserDetails userDetails = null;
        for (EulerUserDetailsProvider provider : this.eulerUserDetailsProviders) {
            if ((userDetails = provider.provide(principal)) != null) {
                break;
            }
        }
        return userDetails;
    }
}
