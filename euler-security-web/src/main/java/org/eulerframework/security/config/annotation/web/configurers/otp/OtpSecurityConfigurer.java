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

import org.eulerframework.security.authentication.otp.OtpChannel;
import org.eulerframework.security.authentication.otp.OtpGenerator;
import org.eulerframework.security.authentication.otp.OtpPolicyResolver;
import org.eulerframework.security.authentication.otp.OtpRecipientResolver;
import org.eulerframework.security.authentication.otp.OtpTicketIssueAuthenticationProvider;
import org.eulerframework.security.authentication.otp.OtpTicketService;
import org.eulerframework.security.web.authentication.otp.OtpTicketIssueAuthenticationConverter;
import org.eulerframework.security.web.authentication.otp.OtpTicketIssueEndpointFilter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

/**
 * An {@link AbstractHttpConfigurer} for the OTP ticket issue endpoint.
 * <p>
 * Registers an {@link OtpTicketIssueEndpointFilter} into the security filter
 * chain. The endpoint is anonymous and CSRF-exempt.
 *
 * <h2>Endpoint</h2>
 * <ul>
 *     <li>{@code POST /otp/tickets} (default, configurable via
 *         {@link #issueEndpointUri(String)})</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>
 * http.with(new OtpSecurityConfigurer(), otp -&gt; otp
 *     .otpChannel(otpChannel)
 *     .recipientResolver(recipientResolver)
 *     .ticketService(ticketService)
 *     .otpGenerator(otpGenerator)
 *     .policyResolver(policyResolver)
 * );
 * </pre>
 * Any dependency that is not explicitly set is resolved from the application
 * context as a single bean of the corresponding type, except for
 * {@link OtpRecipientResolver}, which is optional - when absent, requests
 * carrying {@code identity_id} are rejected with {@code invalid_identity_id}.
 *
 * @see OtpTicketIssueEndpointFilter
 * @see OtpTicketIssueAuthenticationProvider
 */
public class OtpSecurityConfigurer
        extends AbstractHttpConfigurer<OtpSecurityConfigurer, HttpSecurity> {

    public static final String DEFAULT_ISSUE_ENDPOINT_URI = "/otp/tickets";

    private OtpChannel otpChannel;
    private OtpRecipientResolver recipientResolver;
    private OtpTicketService ticketService;
    private OtpGenerator otpGenerator;
    private OtpPolicyResolver policyResolver;
    private String issueEndpointUri = DEFAULT_ISSUE_ENDPOINT_URI;
    private boolean pkceRequired = false;

    private RequestMatcher endpointsMatcher;

    // ---- Fluent API ----

    public OtpSecurityConfigurer otpChannel(OtpChannel otpChannel) {
        this.otpChannel = otpChannel;
        return this;
    }

    public OtpSecurityConfigurer recipientResolver(OtpRecipientResolver recipientResolver) {
        this.recipientResolver = recipientResolver;
        return this;
    }

    public OtpSecurityConfigurer ticketService(OtpTicketService ticketService) {
        this.ticketService = ticketService;
        return this;
    }

    public OtpSecurityConfigurer otpGenerator(OtpGenerator otpGenerator) {
        this.otpGenerator = otpGenerator;
        return this;
    }

    public OtpSecurityConfigurer policyResolver(OtpPolicyResolver policyResolver) {
        this.policyResolver = policyResolver;
        return this;
    }

    public OtpSecurityConfigurer issueEndpointUri(String issueEndpointUri) {
        Assert.hasText(issueEndpointUri, "issueEndpointUri must not be empty");
        this.issueEndpointUri = issueEndpointUri;
        return this;
    }

    /**
     * Whether PKCE (RFC 7636) is required at the issue endpoint. When
     * {@code false} (default), {@code code_challenge} /
     * {@code code_challenge_method} are ignored on the request and not
     * persisted on the ticket. The token endpoint counterpart
     * ({@code grant_type=otp}) must be configured with the same value.
     */
    public OtpSecurityConfigurer pkceRequired(boolean pkceRequired) {
        this.pkceRequired = pkceRequired;
        return this;
    }

    /**
     * Returns a {@link RequestMatcher} that matches all OTP endpoints exposed
     * by this configurer. May be used externally to broaden security rules.
     */
    public RequestMatcher getEndpointsMatcher() {
        return (request) -> this.endpointsMatcher != null && this.endpointsMatcher.matches(request);
    }

    @Override
    public void init(HttpSecurity http) {
        OtpTicketIssueEndpointFilter filter = new OtpTicketIssueEndpointFilter(
                new OtpTicketIssueAuthenticationConverter(this.pkceRequired),
                createProvider(http),
                this.issueEndpointUri);

        this.endpointsMatcher = filter.getRequestMatcher();

        // Anonymous + CSRF-exempt
        http.csrf(csrf -> csrf.ignoringRequestMatchers(this.endpointsMatcher));

        http.setSharedObject(OtpTicketIssueEndpointFilter.class, filter);
    }

    @Override
    public void configure(HttpSecurity http) {
        OtpTicketIssueEndpointFilter filter =
                http.getSharedObject(OtpTicketIssueEndpointFilter.class);
        http.addFilterBefore(postProcess(filter), AuthorizationFilter.class);
    }

    // ---- Dependency resolution ----

    private OtpTicketIssueAuthenticationProvider createProvider(HttpSecurity http) {
        return new OtpTicketIssueAuthenticationProvider(
                resolvePolicyResolver(http),
                resolveOtpGenerator(http),
                resolveOtpChannel(http),
                resolveTicketService(http),
                resolveRecipientResolver(http));
    }

    private OtpChannel resolveOtpChannel(HttpSecurity http) {
        if (this.otpChannel != null) {
            return this.otpChannel;
        }
        return http.getSharedObject(ApplicationContext.class).getBean(OtpChannel.class);
    }

    private OtpTicketService resolveTicketService(HttpSecurity http) {
        if (this.ticketService != null) {
            return this.ticketService;
        }
        return http.getSharedObject(ApplicationContext.class).getBean(OtpTicketService.class);
    }

    private OtpGenerator resolveOtpGenerator(HttpSecurity http) {
        if (this.otpGenerator != null) {
            return this.otpGenerator;
        }
        return http.getSharedObject(ApplicationContext.class).getBean(OtpGenerator.class);
    }

    private OtpPolicyResolver resolvePolicyResolver(HttpSecurity http) {
        if (this.policyResolver != null) {
            return this.policyResolver;
        }
        return http.getSharedObject(ApplicationContext.class).getBean(OtpPolicyResolver.class);
    }

    /**
     * {@link OtpRecipientResolver} is optional - returns {@code null} when no
     * bean is registered. Requests carrying {@code identity_id} will then be
     * rejected with {@code invalid_identity_id}.
     */
    private OtpRecipientResolver resolveRecipientResolver(HttpSecurity http) {
        if (this.recipientResolver != null) {
            return this.recipientResolver;
        }
        try {
            return http.getSharedObject(ApplicationContext.class).getBean(OtpRecipientResolver.class);
        } catch (NoSuchBeanDefinitionException ignored) {
            return null;
        }
    }
}
