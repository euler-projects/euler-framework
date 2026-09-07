/*
 * Copyright 2013-present the original author or authors.
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
package org.eulerframework.security.config.annotation.web.configurers.login;

import org.eulerframework.security.web.authentication.login.LoginMethodsEndpointFilter;
import org.eulerframework.security.web.login.LoginMethodService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.Assert;

/**
 * Registers {@link LoginMethodsEndpointFilter} into the chain. Applying
 * this configurer is the whole enablement: a deployment that leaves it out
 * (the autoconfiguration skips it when the endpoint is disabled) gets no
 * filter and no endpoint at all, rather than a filter that passes through.
 */
public class LoginMethodsConfigurer
        extends AbstractHttpConfigurer<LoginMethodsConfigurer, HttpSecurity> {

    private String loginMethodsEndpointUri;
    private LoginMethodService loginMethodService;

    public LoginMethodsConfigurer loginMethodsEndpointUri(String uri) {
        Assert.hasText(uri, "loginMethodsEndpointUri must not be empty");
        this.loginMethodsEndpointUri = uri;
        return this;
    }

    public LoginMethodsConfigurer loginMethodService(LoginMethodService loginMethodService) {
        Assert.notNull(loginMethodService, "loginMethodService is required");
        this.loginMethodService = loginMethodService;
        return this;
    }

    @Override
    public void configure(HttpSecurity http) {
        Assert.hasText(this.loginMethodsEndpointUri, "loginMethodsEndpointUri not set");
        Assert.notNull(this.loginMethodService, "loginMethodService not set");

        LoginMethodsEndpointFilter filter = new LoginMethodsEndpointFilter(
                this.loginMethodsEndpointUri,
                this.loginMethodService);
        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
    }
}
