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
import org.eulerframework.security.web.authentication.SecurityJsonResponses;
import org.eulerframework.security.web.login.DefaultLoginMethodService;
import org.eulerframework.security.web.login.DefaultLoginMethodService.ResolvedLoginMethod;
import org.eulerframework.security.web.login.LoginMethodDispatch;
import org.eulerframework.security.web.login.LoginMethodHandler;
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
 * {@link LoginMethodHandler#dispatch}
 * method.
 *
 * <p>Requests without the method parameter are passed through
 * unchanged (backward-compatible with plain formLogin when the URLs
 * happen to coincide).
 *
 * <p>This filter is a backend convenience for the reference login
 * page. SPAs that route submissions independently may disable it via
 * {@code euler.security.web.endpoint.login-methods.dispatch.enabled=false}.
 *
 * <p>Redirect outcomes honour the same content negotiation as the login
 * processing endpoints: a client sending {@code Accept: application/json}
 * receives the incomplete-submission redirect as {@code 200} with a
 * {@code redirect_url} body (fetch cannot read a {@code 302} Location),
 * and an unknown method name as the {@code invalid_request} error
 * envelope. The {@code 307} replay stays a real redirect even for JSON
 * clients: fetch follows it and resubmits the body verbatim, landing on
 * the processing endpoint's own JSON envelope.
 */
public class LoginMethodDispatchFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String ERROR_INVALID_REQUEST = "invalid_request";

    private final RequestMatcher requestMatcher;
    private final DefaultLoginMethodService loginMethodService;
    private final String loginPageUrl;
    private final String methodParameter;

    public LoginMethodDispatchFilter(String loginMethodProcessingUrl,
                                    String loginPageUrl,
                                    String methodParameter,
                                    DefaultLoginMethodService loginMethodService) {
        this.requestMatcher = PathPatternRequestMatcher.withDefaults().matcher(
                org.springframework.http.HttpMethod.POST, loginMethodProcessingUrl);
        this.loginPageUrl = loginPageUrl;
        this.methodParameter = methodParameter;
        this.loginMethodService = loginMethodService;
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

        ResolvedLoginMethod resolved = this.loginMethodService.resolve(methodName);
        if (resolved == null) {
            this.logger.warn("Unknown login method '{}' requested; rejecting.", methodName);
            if (SecurityJsonResponses.prefersJson(request)) {
                SecurityJsonResponses.writeError(response, HttpStatus.BAD_REQUEST, ERROR_INVALID_REQUEST);
            } else {
                response.sendRedirect(this.loginPageUrl + "?error");
            }
            return;
        }

        LoginMethodDispatch dispatch = resolved.handler().dispatch(resolved.method(), request);
        executeDispatch(request, dispatch, response);
    }

    private void executeDispatch(HttpServletRequest request, LoginMethodDispatch dispatch,
                                 HttpServletResponse response) throws IOException {
        switch (dispatch.getAction()) {
            case REDIRECT_302 -> {
                if (SecurityJsonResponses.prefersJson(request)) {
                    SecurityJsonResponses.writeSuccess(response, dispatch.getLocation());
                } else {
                    response.sendRedirect(dispatch.getLocation());
                }
            }
            case REDIRECT_307 -> {
                response.setStatus(HttpStatus.TEMPORARY_REDIRECT.value());
                response.setHeader("Location", dispatch.getLocation());
            }
            case NOT_IMPLEMENTED -> response.sendError(HttpStatus.NOT_IMPLEMENTED.value());
        }
    }
}
