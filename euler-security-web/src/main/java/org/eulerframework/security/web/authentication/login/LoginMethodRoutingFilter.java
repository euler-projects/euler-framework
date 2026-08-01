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
package org.eulerframework.security.web.authentication.login;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.security.web.endpoint.user.login.LoginMethodConfigDrivenContributor;
import org.eulerframework.security.web.endpoint.user.login.LoginMethodConfigDrivenContributor.ResolvedLoginMethod;
import org.eulerframework.security.web.endpoint.user.login.LoginMethodDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Security filter that intercepts
 * {@code POST {login-method-processing-url}} requests carrying the
 * configured method parameter and delegates to the corresponding
 * {@link org.eulerframework.security.web.endpoint.user.login.LoginMethodHandler#dispatch}
 * method.
 *
 * <p>Requests without the method parameter are passed through
 * unchanged (backward-compatible with plain formLogin when the URLs
 * happen to coincide).
 *
 * <p>This filter is a backend convenience for the reference login
 * page. SPAs that route submissions independently may disable it via
 * {@code euler.security.web.endpoint.login-method-dispatch.enabled=false}.
 */
public class LoginMethodRoutingFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RequestMatcher requestMatcher;
    private final LoginMethodConfigDrivenContributor contributor;
    private final String loginPageUrl;
    private final String methodParameter;

    public LoginMethodRoutingFilter(String loginMethodProcessingUrl,
                                    String loginPageUrl,
                                    String methodParameter,
                                    LoginMethodConfigDrivenContributor contributor) {
        this.requestMatcher = PathPatternRequestMatcher.withDefaults().matcher(
                org.springframework.http.HttpMethod.POST, loginMethodProcessingUrl);
        this.loginPageUrl = loginPageUrl;
        this.methodParameter = methodParameter;
        this.contributor = contributor;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!this.requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String methodName = request.getParameter(this.methodParameter);
        if (methodName == null || methodName.isEmpty()) {
            // No method parameter: pass through (backward-compatible
            // with plain formLogin when URLs coincide).
            filterChain.doFilter(request, response);
            return;
        }

        ResolvedLoginMethod resolved = this.contributor.resolve(methodName);
        if (resolved == null) {
            this.logger.warn("Unknown login method '{}' requested; redirecting to login page.", methodName);
            response.sendRedirect(this.loginPageUrl + "?error");
            return;
        }

        LoginMethodDispatch dispatch = resolved.handler().dispatch(methodName, resolved.method(), request);
        executeDispatch(dispatch, response);
    }

    private void executeDispatch(LoginMethodDispatch dispatch, HttpServletResponse response) throws IOException {
        switch (dispatch.getAction()) {
            case REDIRECT_302 -> response.sendRedirect(dispatch.getLocation());
            case REDIRECT_307 -> {
                response.setStatus(HttpStatus.TEMPORARY_REDIRECT.value());
                response.setHeader("Location", dispatch.getLocation());
            }
            case NOT_IMPLEMENTED -> response.sendError(HttpStatus.NOT_IMPLEMENTED.value());
        }
    }
}
