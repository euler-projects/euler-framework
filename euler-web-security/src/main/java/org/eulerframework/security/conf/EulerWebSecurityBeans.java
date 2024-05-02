/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.security.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eulerframework.web.module.authentication.extend.EulerAccessDeniedHandler;
import org.eulerframework.web.module.authentication.extend.EulerLoginUrlAuthenticationEntryPoint;
import org.eulerframework.web.module.authentication.extend.EulerUrlAuthenticationFailureHandler;
import org.eulerframework.web.module.authentication.filter.CaptchaUsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class EulerWebSecurityBeans {
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new EulerAccessDeniedHandler();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return new EulerLoginUrlAuthenticationEntryPoint(
                SecurityConfig.getLoginPage(),
                objectMapper
        );
    }

    @Bean
    public AntPathRequestMatcher requiresAuthenticationRequestMatcher() {
        return new AntPathRequestMatcher(
                SecurityConfig.getLoginProcessingUrl(),
                "POST"
        );
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler authenticationSuccessHandler = new SimpleUrlAuthenticationSuccessHandler();
        authenticationSuccessHandler.setDefaultTargetUrl(SecurityConfig.getLoginDefaultTargetUrl());
        authenticationSuccessHandler.setTargetUrlParameter("redirectUrl");
        return authenticationSuccessHandler;
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new EulerUrlAuthenticationFailureHandler();
    }

    @Bean
    public UsernamePasswordAuthenticationFilter formLoginFilter(AuthenticationManager authenticationManager, AntPathRequestMatcher requiresAuthenticationRequestMatcher) {
        CaptchaUsernamePasswordAuthenticationFilter captchaUsernamePasswordAuthenticationFilter = new CaptchaUsernamePasswordAuthenticationFilter();
        captchaUsernamePasswordAuthenticationFilter.setEnableCaptcha(SecurityConfig.isSignUpEnableCaptcha());
        captchaUsernamePasswordAuthenticationFilter.setAuthenticationManager(authenticationManager);
        captchaUsernamePasswordAuthenticationFilter.setRequiresAuthenticationRequestMatcher(requiresAuthenticationRequestMatcher);

        return captchaUsernamePasswordAuthenticationFilter;
    }
}
