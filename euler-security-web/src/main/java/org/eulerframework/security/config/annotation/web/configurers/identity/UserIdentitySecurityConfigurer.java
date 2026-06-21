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
package org.eulerframework.security.config.annotation.web.configurers.identity;

import org.eulerframework.security.core.identity.UserIdentityService;
import org.eulerframework.security.core.userdetails.EulerUserDetailsService;
import org.eulerframework.security.web.authentication.identity.UserIdentityEndpointFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

/**
 * {@link AbstractHttpConfigurer} that installs the
 * {@code /user/identities} REST surface into a Spring Security filter
 * chain by registering a {@link UserIdentityEndpointFilter}.
 *
 * <p>The endpoints require an authenticated user; access-token
 * validation is handled by the surrounding filter chain (typically the
 * authorization-server chain's {@code oauth2ResourceServer.jwt()}).
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code POST   <baseUri>}              &mdash; bind a new identity</li>
 *   <li>{@code GET    <baseUri>}              &mdash; list identities owned by the caller</li>
 *   <li>{@code GET    <baseUri>/{identityId}} &mdash; read a single identity</li>
 *   <li>{@code PUT    <baseUri>/{identityId}} &mdash; replace an identity</li>
 *   <li>{@code DELETE <baseUri>/{identityId}} &mdash; delete an identity</li>
 * </ul>
 *
 * <p>The base URI defaults to {@link #DEFAULT_ENDPOINT_BASE_URI};
 * override via {@link #endpointBaseUri(String)}.
 *
 * <h2>Usage</h2>
 * <pre>
 * http.with(new UserIdentitySecurityConfigurer(), identity -&gt; identity
 *     .userIdentityService(userIdentityService));
 * </pre>
 * <p>The {@link UserIdentityService} and {@link EulerUserDetailsService}
 * dependencies, when not set explicitly, are resolved from the
 * application context as single beans.
 *
 * @see UserIdentityEndpointFilter
 * @see UserIdentityService
 */
public class UserIdentitySecurityConfigurer
        extends AbstractHttpConfigurer<UserIdentitySecurityConfigurer, HttpSecurity> {

    /**
     * Default base URI of the endpoint family, matching the documented
     * {@code /user/identities} client contract.
     */
    public static final String DEFAULT_ENDPOINT_BASE_URI = "/user/identities";

    private UserIdentityService userIdentityService;
    private EulerUserDetailsService userDetailsService;
    private String endpointBaseUri = DEFAULT_ENDPOINT_BASE_URI;

    private RequestMatcher endpointsMatcher;

    // ---- Fluent API ----

    public UserIdentitySecurityConfigurer userIdentityService(
            UserIdentityService userIdentityService) {
        this.userIdentityService = userIdentityService;
        return this;
    }

    public UserIdentitySecurityConfigurer userDetailsService(
            EulerUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        return this;
    }

    public UserIdentitySecurityConfigurer endpointBaseUri(String endpointBaseUri) {
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
        UserIdentityEndpointFilter filter = new UserIdentityEndpointFilter(
                resolveUserIdentityService(http),
                resolveUserDetailsService(http),
                this.endpointBaseUri);

        this.endpointsMatcher = filter.getRequestMatcher();

        // The token-protected endpoints are stateless; exempt them from CSRF.
        http.csrf(csrf -> csrf.ignoringRequestMatchers(this.endpointsMatcher));

        http.setSharedObject(UserIdentityEndpointFilter.class, filter);
    }

    @Override
    public void configure(HttpSecurity http) {
        UserIdentityEndpointFilter filter =
                http.getSharedObject(UserIdentityEndpointFilter.class);
        http.addFilterBefore(postProcess(filter), AuthorizationFilter.class);
    }

    // ---- Dependency resolution ----

    private UserIdentityService resolveUserIdentityService(HttpSecurity http) {
        if (this.userIdentityService != null) {
            return this.userIdentityService;
        }
        return http.getSharedObject(ApplicationContext.class).getBean(UserIdentityService.class);
    }

    private EulerUserDetailsService resolveUserDetailsService(HttpSecurity http) {
        if (this.userDetailsService != null) {
            return this.userDetailsService;
        }
        return http.getSharedObject(ApplicationContext.class).getBean(EulerUserDetailsService.class);
    }
}
