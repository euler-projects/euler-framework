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
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
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

    private String loginPage;
    private String targetUrlParameter;
    private String defaultTargetUrl;
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
        AuthenticationSuccessHandler handler = resolveSuccessHandler(http);
        String loginPage = this.loginPage;
        try {
            http.oauth2Login(oauth2 -> {
                if (loginPage != null) {
                    oauth2.loginPage(loginPage);
                }
                oauth2.successHandler(handler);
            });
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to configure oauth2Login()", ex);
        }
    }

    @Override
    public void configure(HttpSecurity http) {
        // Nothing to add beyond the init-time oauth2Login() wiring; the
        // OAuth2LoginConfigurer configures its own filters.
    }

    // ---- Dependency resolution ----

    private AuthenticationSuccessHandler resolveSuccessHandler(HttpSecurity http) {
        if (this.successHandler != null) {
            applyRedirectOverrides(this.successHandler);
            return this.successHandler;
        }
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        OAuth2LoginPrincipalPromotingSuccessHandler handler;
        try {
            handler = context.getBean(OAuth2LoginPrincipalPromotingSuccessHandler.class);
        } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException notFound) {
            handler = new OAuth2LoginPrincipalPromotingSuccessHandler(
                    context.getBean(EulerUserService.class),
                    context.getBean(UserIdentityService.class));
        }
        applyRedirectOverrides(handler);
        return handler;
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
