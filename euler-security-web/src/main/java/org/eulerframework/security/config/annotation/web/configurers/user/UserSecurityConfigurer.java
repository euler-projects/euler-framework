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
package org.eulerframework.security.config.annotation.web.configurers.user;

import org.eulerframework.security.core.userdetails.EulerUserDetailsService;
import org.eulerframework.security.web.authentication.user.UserEndpointFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

/**
 * {@link AbstractHttpConfigurer} that installs the {@code /user} REST
 * surface into a Spring Security filter chain by registering a
 * {@link UserEndpointFilter}.
 *
 * <p>The endpoints require an authenticated user; access-token
 * validation is handled by the surrounding filter chain (typically the
 * authorization-server chain's {@code oauth2ResourceServer.jwt()}).
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code DELETE <baseUri>} &mdash; delete the caller's own
 *       account via {@link UserDetailsManager#deleteUser(String)}</li>
 * </ul>
 *
 * <p>The base URI defaults to {@link #DEFAULT_ENDPOINT_BASE_URI};
 * override via {@link #endpointBaseUri(String)}.
 *
 * <h2>Usage</h2>
 * <pre>
 * http.with(new UserSecurityConfigurer(), user -&gt; user
 *     .userDetailsManager(userDetailsManager));
 * </pre>
 * <p>The {@link UserDetailsManager} and {@link EulerUserDetailsService}
 * dependencies, when not set explicitly, are resolved from the
 * application context as single beans.
 *
 * @see UserEndpointFilter
 * @see UserDetailsManager
 */
public class UserSecurityConfigurer
        extends AbstractHttpConfigurer<UserSecurityConfigurer, HttpSecurity> {

    /**
     * Default base URI of the endpoint family.
     */
    public static final String DEFAULT_ENDPOINT_BASE_URI = "/user";

    private UserDetailsManager userDetailsManager;
    private EulerUserDetailsService userDetailsService;
    private String endpointBaseUri = DEFAULT_ENDPOINT_BASE_URI;

    private RequestMatcher endpointsMatcher;

    // ---- Fluent API ----

    public UserSecurityConfigurer userDetailsManager(UserDetailsManager userDetailsManager) {
        this.userDetailsManager = userDetailsManager;
        return this;
    }

    public UserSecurityConfigurer userDetailsService(EulerUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        return this;
    }

    public UserSecurityConfigurer endpointBaseUri(String endpointBaseUri) {
        Assert.hasText(endpointBaseUri, "endpointBaseUri must not be empty");
        Assert.isTrue(!endpointBaseUri.endsWith("/"), "endpointBaseUri must not end with '/'");
        this.endpointBaseUri = endpointBaseUri;
        return this;
    }

    /**
     * Returns a {@link RequestMatcher} matching every endpoint installed
     * by this configurer. Compose with {@code http.securityMatcher(...)}
     * via an {@code OrRequestMatcher} so the surrounding chain's
     * authentication rules cover these endpoints.
     */
    public RequestMatcher getEndpointsMatcher() {
        return (request) -> this.endpointsMatcher != null && this.endpointsMatcher.matches(request);
    }

    @Override
    public void init(HttpSecurity http) {
        UserEndpointFilter filter = new UserEndpointFilter(
                resolveUserDetailsManager(http),
                resolveUserDetailsService(http),
                this.endpointBaseUri);

        this.endpointsMatcher = filter.getRequestMatcher();

        // The token-protected endpoints are stateless; exempt them from CSRF.
        http.csrf(csrf -> csrf.ignoringRequestMatchers(this.endpointsMatcher));

        http.setSharedObject(UserEndpointFilter.class, filter);
    }

    @Override
    public void configure(HttpSecurity http) {
        UserEndpointFilter filter = http.getSharedObject(UserEndpointFilter.class);
        http.addFilterBefore(postProcess(filter), AuthorizationFilter.class);
    }

    // ---- Dependency resolution ----

    private UserDetailsManager resolveUserDetailsManager(HttpSecurity http) {
        if (this.userDetailsManager != null) {
            return this.userDetailsManager;
        }
        return http.getSharedObject(ApplicationContext.class).getBean(UserDetailsManager.class);
    }

    private EulerUserDetailsService resolveUserDetailsService(HttpSecurity http) {
        if (this.userDetailsService != null) {
            return this.userDetailsService;
        }
        return http.getSharedObject(ApplicationContext.class).getBean(EulerUserDetailsService.class);
    }
}
