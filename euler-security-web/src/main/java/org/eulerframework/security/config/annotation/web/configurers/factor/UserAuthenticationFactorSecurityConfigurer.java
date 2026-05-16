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
package org.eulerframework.security.config.annotation.web.configurers.factor;

import org.eulerframework.security.authentication.factor.UserAuthenticationFactorService;
import org.eulerframework.security.core.userdetails.EulerUserDetailsService;
import org.eulerframework.security.web.authentication.factor.UserAuthenticationFactorEndpointFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

/**
 * An {@link AbstractHttpConfigurer} for the {@code /user/identities} REST
 * surface.
 * <p>
 * Registers a {@link UserAuthenticationFactorEndpointFilter} into the
 * security filter chain. The endpoints require an authenticated user; AT
 * validation is the responsibility of the surrounding filter chain
 * (typically the authorization-server chain's
 * {@code oauth2ResourceServer.jwt()}).
 *
 * <h2>Endpoints</h2>
 * <ul>
 *     <li>{@code POST   <baseUri>}        - bind a new factor</li>
 *     <li>{@code GET    <baseUri>}        - list this user's factors</li>
 *     <li>{@code GET    <baseUri>/{id}}   - get one factor</li>
 *     <li>{@code DELETE <baseUri>/{id}}   - delete one factor</li>
 * </ul>
 * The base URI defaults to {@link #DEFAULT_ENDPOINT_BASE_URI} which is kept
 * intentionally aligned with the {@code Model-#-User-Identity} v2 client
 * contract; rename it via {@link #endpointBaseUri(String)}.
 *
 * <h2>Usage</h2>
 * <pre>
 * http.with(new UserAuthenticationFactorSecurityConfigurer(), factor -&gt; factor
 *     .userAuthenticationService(userAuthenticationFactorService));
 * </pre>
 * The {@link UserAuthenticationFactorService} and {@link EulerUserDetailsService}
 * dependencies, if not explicitly set, are resolved from the application
 * context as single beans.
 *
 * @see UserAuthenticationFactorEndpointFilter
 * @see UserAuthenticationFactorService
 */
public class UserAuthenticationFactorSecurityConfigurer
        extends AbstractHttpConfigurer<UserAuthenticationFactorSecurityConfigurer, HttpSecurity> {

    /**
     * Default base URI of the endpoint family. Aligned with the existing
     * {@code /user/identities} client contract.
     */
    public static final String DEFAULT_ENDPOINT_BASE_URI = "/user/identities";

    private UserAuthenticationFactorService userAuthenticationFactorService;
    private EulerUserDetailsService userDetailsService;
    private String endpointBaseUri = DEFAULT_ENDPOINT_BASE_URI;

    private RequestMatcher endpointsMatcher;

    // ---- Fluent API ----

    public UserAuthenticationFactorSecurityConfigurer userAuthenticationService(
            UserAuthenticationFactorService userAuthenticationFactorService) {
        this.userAuthenticationFactorService = userAuthenticationFactorService;
        return this;
    }

    public UserAuthenticationFactorSecurityConfigurer userDetailsService(
            EulerUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        return this;
    }

    public UserAuthenticationFactorSecurityConfigurer endpointBaseUri(String endpointBaseUri) {
        Assert.hasText(endpointBaseUri, "endpointBaseUri must not be empty");
        Assert.isTrue(!endpointBaseUri.endsWith("/"), "endpointBaseUri must not end with '/'");
        this.endpointBaseUri = endpointBaseUri;
        return this;
    }

    /**
     * Returns a {@link RequestMatcher} that matches all endpoints exposed
     * by this configurer. Typically combined with {@code http.securityMatcher(...)}
     * via an {@code OrRequestMatcher} so that the surrounding chain's
     * security rules (e.g. {@code oauth2ResourceServer.jwt()}) cover these
     * endpoints.
     */
    public RequestMatcher getEndpointsMatcher() {
        return (request) -> this.endpointsMatcher != null && this.endpointsMatcher.matches(request);
    }

    @Override
    public void init(HttpSecurity http) {
        UserAuthenticationFactorEndpointFilter filter = new UserAuthenticationFactorEndpointFilter(
                resolveUserAuthenticationService(http),
                resolveUserDetailsService(http),
                this.endpointBaseUri);

        this.endpointsMatcher = filter.getRequestMatcher();

        // CSRF-exempt: the AT-protected endpoints are not session-based.
        http.csrf(csrf -> csrf.ignoringRequestMatchers(this.endpointsMatcher));

        http.setSharedObject(UserAuthenticationFactorEndpointFilter.class, filter);
    }

    @Override
    public void configure(HttpSecurity http) {
        UserAuthenticationFactorEndpointFilter filter =
                http.getSharedObject(UserAuthenticationFactorEndpointFilter.class);
        http.addFilterBefore(postProcess(filter), AuthorizationFilter.class);
    }

    // ---- Dependency resolution ----

    private UserAuthenticationFactorService resolveUserAuthenticationService(HttpSecurity http) {
        if (this.userAuthenticationFactorService != null) {
            return this.userAuthenticationFactorService;
        }
        return http.getSharedObject(ApplicationContext.class).getBean(UserAuthenticationFactorService.class);
    }

    private EulerUserDetailsService resolveUserDetailsService(HttpSecurity http) {
        if (this.userDetailsService != null) {
            return this.userDetailsService;
        }
        return http.getSharedObject(ApplicationContext.class).getBean(EulerUserDetailsService.class);
    }
}
