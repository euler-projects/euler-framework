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

import org.eulerframework.security.web.authentication.login.LoginMethodDispatchFilter;
import org.eulerframework.security.web.login.DefaultLoginMethodService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.Assert;

/**
 * {@link AbstractHttpConfigurer} that registers a
 * {@link LoginMethodDispatchFilter} into the security filter chain,
 * positioned before {@link UsernamePasswordAuthenticationFilter}.
 *
 * <p>Gated by the
 * {@code euler.security.web.endpoint.login-methods.dispatch.enabled}
 * property; when disabled, the autoconfigure layer simply does not
 * apply this configurer and no filter is registered.
 */
public class LoginMethodDispatchConfigurer
        extends AbstractHttpConfigurer<LoginMethodDispatchConfigurer, HttpSecurity> {

    private String loginMethodProcessingUrl;
    private String loginPageUrl;
    private String methodParameter;
    private DefaultLoginMethodService loginMethodService;

    public LoginMethodDispatchConfigurer loginMethodProcessingUrl(String url) {
        Assert.hasText(url, "loginMethodProcessingUrl must not be empty");
        this.loginMethodProcessingUrl = url;
        return this;
    }

    public LoginMethodDispatchConfigurer loginPageUrl(String url) {
        Assert.hasText(url, "loginPageUrl must not be empty");
        this.loginPageUrl = url;
        return this;
    }

    public LoginMethodDispatchConfigurer methodParameter(String methodParameter) {
        Assert.hasText(methodParameter, "methodParameter must not be empty");
        this.methodParameter = methodParameter;
        return this;
    }

    public LoginMethodDispatchConfigurer loginMethodService(DefaultLoginMethodService loginMethodService) {
        Assert.notNull(loginMethodService, "loginMethodService is required");
        this.loginMethodService = loginMethodService;
        return this;
    }

    @Override
    public void configure(HttpSecurity http) {
        Assert.hasText(this.loginMethodProcessingUrl, "loginMethodProcessingUrl not set");
        Assert.hasText(this.loginPageUrl, "loginPageUrl not set");
        Assert.hasText(this.methodParameter, "methodParameter not set");
        Assert.notNull(this.loginMethodService, "loginMethodService not set");

        LoginMethodDispatchFilter filter = new LoginMethodDispatchFilter(
                this.loginMethodProcessingUrl,
                this.loginPageUrl,
                this.methodParameter,
                this.loginMethodService);
        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
    }
}
