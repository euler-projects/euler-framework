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

package org.eulerframework.security.config.annotation.web.configurers.otp;

import org.eulerframework.security.authentication.otp.OneTimePasswordChannel;
import org.eulerframework.security.authentication.otp.OneTimePasswordPolicyResolver;
import org.eulerframework.security.authentication.otp.OneTimePasswordRecipientResolver;
import org.eulerframework.security.authentication.otp.OneTimePasswordTestAccountSupport;
import org.eulerframework.security.authentication.otp.OneTimePasswordIssueAuthenticationProvider;
import org.eulerframework.security.authentication.otp.OneTimePasswordService;
import org.eulerframework.security.web.authentication.otp.OneTimePasswordAuthenticationFilter;
import org.eulerframework.security.web.authentication.otp.OneTimePasswordIssueAuthenticationConverter;
import org.eulerframework.security.web.authentication.otp.OneTimePasswordIssueEndpointFilter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.FilterOrderRegistrationAccessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.ott.OneTimeTokenAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

/**
 * An {@link AbstractAuthenticationFilterConfigurer} for one-time-password
 * (verification-code) login.
 * <p>
 * Applying this configurer activates both halves of the two-step OTP flow:
 *
 * <h2>Ticket issue endpoint</h2>
 * <ul>
 *     <li>{@code POST /otp/tickets} (default, configurable via
 *         {@link #issueEndpointUri(String)}), anonymous and CSRF-exempt,
 *         served by {@link OneTimePasswordIssueEndpointFilter}</li>
 * </ul>
 *
 * <h2>Login endpoint</h2>
 * <ul>
 *     <li>{@code POST /login/otp} (default, configurable via
 *         {@link #loginProcessingUrl(String)}), served by
 *         {@link OneTimePasswordAuthenticationFilter}; the submission
 *         carries {@code otp_ticket} + {@code otp}</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>
 * http.with(new OneTimePasswordLoginConfigurer(), otp -&gt; otp
 *     .loginPage("/login")
 *     .oneTimePasswordChannel(oneTimePasswordChannel)
 *     .recipientResolver(recipientResolver)
 *     .ticketService(ticketService)
 *     .policyResolver(policyResolver)
 * );
 * </pre>
 * Any issue-endpoint dependency that is not explicitly set is resolved from
 * the application context as a single bean of the corresponding type, except
 * for {@link OneTimePasswordRecipientResolver}, which is optional - when absent, requests
 * carrying {@code identity_id} are rejected with {@code invalid_identity_id}.
 *
 * @see OneTimePasswordAuthenticationFilter
 * @see OneTimePasswordIssueEndpointFilter
 * @see OneTimePasswordIssueAuthenticationProvider
 */
public class OneTimePasswordLoginConfigurer
        extends AbstractAuthenticationFilterConfigurer<HttpSecurity, OneTimePasswordLoginConfigurer, OneTimePasswordAuthenticationFilter> {

    public static final String DEFAULT_ISSUE_ENDPOINT_URI = "/otp/tickets";

    private OneTimePasswordChannel oneTimePasswordChannel;
    private OneTimePasswordRecipientResolver recipientResolver;
    private OneTimePasswordService ticketService;
    private OneTimePasswordPolicyResolver policyResolver;
    private OneTimePasswordTestAccountSupport testAccountSupport;
    private String issueEndpointUri = DEFAULT_ISSUE_ENDPOINT_URI;

    private OneTimePasswordIssueEndpointFilter issueEndpointFilter;

    public OneTimePasswordLoginConfigurer() {
        super(new OneTimePasswordAuthenticationFilter(),
                OneTimePasswordAuthenticationFilter.DEFAULT_LOGIN_PROCESSING_URL);
    }

    // ---- Fluent API: issue endpoint dependencies ----

    public OneTimePasswordLoginConfigurer oneTimePasswordChannel(OneTimePasswordChannel oneTimePasswordChannel) {
        this.oneTimePasswordChannel = oneTimePasswordChannel;
        return this;
    }

    public OneTimePasswordLoginConfigurer recipientResolver(OneTimePasswordRecipientResolver recipientResolver) {
        this.recipientResolver = recipientResolver;
        return this;
    }

    public OneTimePasswordLoginConfigurer ticketService(OneTimePasswordService ticketService) {
        this.ticketService = ticketService;
        return this;
    }

    public OneTimePasswordLoginConfigurer policyResolver(OneTimePasswordPolicyResolver policyResolver) {
        this.policyResolver = policyResolver;
        return this;
    }

    /**
     * Configure the optional test-account whitelist. When set, requests whose
     * resolved recipient matches one of its entries receive the configured
     * fixed OTP and skip real delivery. Pass {@code null} to disable (default).
     */
    public OneTimePasswordLoginConfigurer testAccountSupport(OneTimePasswordTestAccountSupport testAccountSupport) {
        this.testAccountSupport = testAccountSupport;
        return this;
    }

    public OneTimePasswordLoginConfigurer issueEndpointUri(String issueEndpointUri) {
        Assert.hasText(issueEndpointUri, "issueEndpointUri must not be empty");
        this.issueEndpointUri = issueEndpointUri;
        return this;
    }

    // ---- Fluent API: login ----

    /**
     * Specifies the URL to render the login page.
     */
    @Override
    public OneTimePasswordLoginConfigurer loginPage(String loginPage) {
        return (OneTimePasswordLoginConfigurer) super.loginPage(loginPage);
    }

    // ---- Lifecycle ----

    @Override
    public void init(HttpSecurity http) {
        this.issueEndpointFilter = new OneTimePasswordIssueEndpointFilter(
                new OneTimePasswordIssueAuthenticationConverter(),
                createIssueProvider(http),
                this.issueEndpointUri);

        // The issue endpoint is anonymous and CSRF-exempt; the login
        // endpoint itself keeps standard CSRF protection like formLogin.
        http.csrf(csrf -> csrf.ignoringRequestMatchers(this.issueEndpointFilter.getRequestMatcher()));

        super.init(http);
    }

    @Override
    public void configure(HttpSecurity http) {
        // OneTimePasswordAuthenticationFilter is not part of Spring
        // Security's built-in filter order registry; register it right
        // after OneTimeTokenAuthenticationFilter so both one-time
        // credential filters sit adjacent, before super adds it through
        // addFilter.
        FilterOrderRegistrationAccessor.register(http,
                OneTimePasswordAuthenticationFilter.class,
                OneTimeTokenAuthenticationFilter.class, 1);
        super.configure(http);
        http.addFilterBefore(postProcess(this.issueEndpointFilter), AuthorizationFilter.class);
    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return PathPatternRequestMatcher.pathPattern(HttpMethod.POST, loginProcessingUrl);
    }

    /**
     * Returns a {@link RequestMatcher} that matches all endpoints exposed by
     * this configurer - the login processing URL and the ticket issue
     * endpoint. May be used externally to broaden security rules.
     */
    public RequestMatcher getEndpointsMatcher() {
        RequestMatcher loginMatcher = createLoginProcessingUrlMatcher(getLoginProcessingUrl());
        return (request) -> loginMatcher.matches(request)
                || (this.issueEndpointFilter != null
                        && this.issueEndpointFilter.getRequestMatcher().matches(request));
    }

    // ---- Dependency resolution ----

    private OneTimePasswordIssueAuthenticationProvider createIssueProvider(HttpSecurity http) {
        OneTimePasswordIssueAuthenticationProvider provider = new OneTimePasswordIssueAuthenticationProvider(
                resolvePolicyResolver(http),
                resolveOneTimePasswordChannel(http),
                resolveTicketService(http),
                resolveRecipientResolver(http));
        provider.setTestAccountSupport(this.testAccountSupport);
        return provider;
    }

    private OneTimePasswordChannel resolveOneTimePasswordChannel(HttpSecurity http) {
        if (this.oneTimePasswordChannel != null) {
            return this.oneTimePasswordChannel;
        }
        return http.getSharedObject(ApplicationContext.class).getBean(OneTimePasswordChannel.class);
    }

    private OneTimePasswordService resolveTicketService(HttpSecurity http) {
        if (this.ticketService != null) {
            return this.ticketService;
        }
        return http.getSharedObject(ApplicationContext.class).getBean(OneTimePasswordService.class);
    }

    private OneTimePasswordPolicyResolver resolvePolicyResolver(HttpSecurity http) {
        if (this.policyResolver != null) {
            return this.policyResolver;
        }
        return http.getSharedObject(ApplicationContext.class).getBean(OneTimePasswordPolicyResolver.class);
    }

    /**
     * {@link OneTimePasswordRecipientResolver} is optional - returns {@code null} when no
     * bean is registered. Requests carrying {@code identity_id} will then be
     * rejected with {@code invalid_identity_id}.
     */
    private OneTimePasswordRecipientResolver resolveRecipientResolver(HttpSecurity http) {
        if (this.recipientResolver != null) {
            return this.recipientResolver;
        }
        try {
            return http.getSharedObject(ApplicationContext.class).getBean(OneTimePasswordRecipientResolver.class);
        } catch (NoSuchBeanDefinitionException ignored) {
            return null;
        }
    }
}
