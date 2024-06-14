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
package org.eulerframework.security.provisioning;

import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.core.context.UserContext;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.core.userdetails.ProviderEulerUserDetailsService;
import org.eulerframework.security.core.userdetails.provider.EulerUserDetailsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProviderEulerUserDetailsManager extends DefaultEulerUserDetailsManager {
    private final Logger logger = LoggerFactory.getLogger(ProviderEulerUserDetailsManager.class);

    private final ProviderEulerUserDetailsService providerEulerUserDetailsService;

    public ProviderEulerUserDetailsManager(EulerUserService eulerUserService, EulerUserDetailsProvider... eulerUserDetailsProvider) {
        super(eulerUserService);
        this.providerEulerUserDetailsService = new ProviderEulerUserDetailsService(eulerUserService, eulerUserDetailsProvider);
    }

    public ProviderEulerUserDetailsManager(EulerUserService eulerUserService, UserContext userContext, EulerUserDetailsProvider... eulerUserDetailsProvider) {
        super(eulerUserService, userContext);
        this.providerEulerUserDetailsService = new ProviderEulerUserDetailsService(eulerUserService, eulerUserDetailsProvider);
    }

    public ProviderEulerUserDetailsManager(EulerUserService eulerUserService, List<? extends EulerUserDetailsProvider> eulerUserDetailsProviders) {
        super(eulerUserService);
        this.providerEulerUserDetailsService = new ProviderEulerUserDetailsService(eulerUserService, eulerUserDetailsProviders);
    }

    public ProviderEulerUserDetailsManager(EulerUserService eulerUserService, UserContext userContext, List<? extends EulerUserDetailsProvider> eulerUserDetailsProviders) {
        super(eulerUserService, userContext);
        this.providerEulerUserDetailsService = new ProviderEulerUserDetailsService(eulerUserService, eulerUserDetailsProviders);
    }

    @Override
    public EulerUserDetails loadUserByPrincipal(String principal) {
        return this.providerEulerUserDetailsService.loadUserByUsername(principal);
    }
}
