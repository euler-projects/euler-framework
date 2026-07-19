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
package org.eulerframework.security.config.annotation.web.configurers.oauth2;

import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.core.identity.UserIdentityService;
import org.eulerframework.security.oauth2.client.authentication.OAuth2LoginPrincipalPromotingSuccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;

/**
 * An {@link AbstractHttpConfigurer} that wires {@code oauth2Login()}
 * with an {@link OAuth2LoginPrincipalPromotingSuccessHandler} so that
 * federated logins land in the shared login page and end up in the
 * security context as local {@code EulerUserDetails}.
 *
 * <h2>Usage</h2>
 * <pre>
 * http.with(new OAuth2LoginSecurityConfigurer(), oauth2 -&gt; oauth2
 *     .loginPage("/signin")
 *     .targetUrlParameter("returnUrl"));
 * </pre>
 * The success handler is either supplied directly via
 * {@link #successHandler(AuthenticationSuccessHandler)} or resolved from
 * the application context; when neither is available a fresh instance
 * is built using the {@link EulerUserService} / {@link UserIdentityService}
 * beans without any per-registration policy (unknown users are
 * rejected). Per-registration login policies (auto-create-user,
 * default-authorities, identity-type) are configured on the handler
 * bean itself by the autoconfigure layer based on
 * {@code euler.security.web.login-methods.<name>.properties}.
 */
public class OAuth2LoginSecurityConfigurer
        extends AbstractHttpConfigurer<OAuth2LoginSecurityConfigurer, HttpSecurity> {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSecurityConfigurer.class);

    private String loginPage;
    private String targetUrlParameter;
    private String defaultTargetUrl;
    /**
     * The effective success handler. Two-phase field:
     * <ul>
     *   <li>Before {@link #init(HttpSecurity)}: caller-supplied override
     *       via {@link #successHandler(AuthenticationSuccessHandler)},
     *       or {@code null} if the caller wants the default resolution.</li>
     *   <li>After {@link #init(HttpSecurity)}: the resolved handler that
     *       has actually been wired into {@code oauth2Login()}. Never
     *       {@code null}.</li>
     * </ul>
     * {@link #configure(HttpSecurity)} relies on the post-init
     * invariant to skip null-checking.
     */
    private AuthenticationSuccessHandler successHandler;

    // ---- Fluent API ----

    public OAuth2LoginSecurityConfigurer loginPage(String loginPage) {
        Assert.hasText(loginPage, "loginPage must not be empty");
        this.loginPage = loginPage;
        return this;
    }

    public OAuth2LoginSecurityConfigurer targetUrlParameter(String targetUrlParameter) {
        this.targetUrlParameter = targetUrlParameter;
        return this;
    }

    public OAuth2LoginSecurityConfigurer defaultTargetUrl(String defaultTargetUrl) {
        this.defaultTargetUrl = defaultTargetUrl;
        return this;
    }

    public OAuth2LoginSecurityConfigurer successHandler(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
        return this;
    }

    @Override
    public void init(HttpSecurity http) {
        resolveSuccessHandler(http);
        String loginPage = this.loginPage;
        AuthenticationSuccessHandler handler = this.successHandler;
        http.oauth2Login(oauth2 -> {
            if (loginPage != null) {
                oauth2.loginPage(loginPage);
            }
            oauth2.successHandler(handler);
        });
    }

    @Override
    public void configure(HttpSecurity http) {
        // Wire the chain-scoped SecurityContextRepository into the
        // promoting success handler so the promoted principal is
        // persisted through the same pipeline the OAuth2 login filter
        // used to write the original OidcUser. Without this the handler
        // falls back to a fresh HttpSessionSecurityContextRepository,
        // which drops any RequestAttribute mirror and bypasses any
        // custom repository the deployment might have installed (Redis,
        // JDBC, delegating chains, etc.).
        //
        // Runs in configure() rather than init() because
        // SecurityContextConfigurer registers its repository as a
        // shared object during its own configure() phase, so init() may
        // still see null here.
        //
        // successHandler is guaranteed non-null here (init() always
        // resolves it), so we only need to guard the promoting-type
        // narrowing for the case where the caller supplied a fully
        // custom handler.
        if (this.successHandler instanceof OAuth2LoginPrincipalPromotingSuccessHandler promoting) {
            SecurityContextRepository shared = http.getSharedObject(SecurityContextRepository.class);
            if (shared != null) {
                promoting.setSecurityContextRepository(shared);
            } else {
                logger.warn("No shared SecurityContextRepository on HttpSecurity; " +
                        "OAuth2LoginPrincipalPromotingSuccessHandler will keep its default " +
                        "HttpSessionSecurityContextRepository, which may not match the " +
                        "repository used by other filters in this chain.");
            }
        }
    }

    // ---- Dependency resolution ----

    /**
     * Ensure {@link #successHandler} is populated. If the caller
     * already supplied one via the fluent {@code successHandler(...)}
     * setter, keep it; otherwise look one up in the application context
     * (falling back to a freshly constructed default).
     *
     * <p>After this method returns, {@link #successHandler} is
     * guaranteed non-null.
     */
    private void resolveSuccessHandler(HttpSecurity http) {
        if (this.successHandler == null) {
            ApplicationContext context = http.getSharedObject(ApplicationContext.class);
            try {
                this.successHandler = context.getBean(OAuth2LoginPrincipalPromotingSuccessHandler.class);
            } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException notFound) {
                this.successHandler = new OAuth2LoginPrincipalPromotingSuccessHandler(
                        context.getBean(EulerUserService.class),
                        context.getBean(UserIdentityService.class));
            }
        }
        applyRedirectOverrides(this.successHandler);
    }

    private void applyRedirectOverrides(AuthenticationSuccessHandler handler) {
        if (!(handler instanceof OAuth2LoginPrincipalPromotingSuccessHandler promoting)) {
            // Custom success handler supplied by the caller; overrides
            // are intentionally ignored so the caller retains control.
            return;
        }
        if (this.targetUrlParameter != null) {
            promoting.setTargetUrlParameter(this.targetUrlParameter);
        }
        if (this.defaultTargetUrl != null) {
            promoting.setDefaultTargetUrl(this.defaultTargetUrl);
        }
    }
}
