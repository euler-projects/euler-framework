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
import org.eulerframework.common.util.jackson.JacksonUtils;
import org.eulerframework.security.web.login.LoginMethod;
import org.eulerframework.security.web.login.LoginMethodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Filter exposing the login-method list endpoint ({@code GET /login-methods}
 * by default). The endpoint is anonymous: it answers before the
 * authorization step so that a not-yet-signed-in client - typically a
 * standalone SPA shell that cannot receive the list through template
 * injection - learns which methods to render.
 *
 * <h2>Success response (HTTP 200)</h2>
 * <pre>
 * [{"name":"password","type":"password","primary":true,"attributes":{}}]
 * </pre>
 * The body is the bare array of projected methods. Field names follow the
 * snake-case convention of the built-in authentication-service endpoints;
 * the projected fields happen to be single words, so no separator appears
 * in practice.
 *
 * <h2>Error responses</h2>
 * A failure while assembling the list is an internal condition, answered
 * with the {@code error} / {@code error_description} envelope and
 * {@code 500}; clients must not retry it as a credential problem.
 */
public class LoginMethodsEndpointFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoginMethodsEndpointFilter.class);

    private static final String ERROR_SERVER_ERROR = "server_error";

    private final LoginMethodService loginMethodService;
    private final RequestMatcher requestMatcher;

    public LoginMethodsEndpointFilter(String endpointUri, LoginMethodService loginMethodService) {
        Assert.hasText(endpointUri, "endpointUri must not be empty");
        Assert.notNull(loginMethodService, "loginMethodService must not be null");
        this.loginMethodService = loginMethodService;
        this.requestMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.GET, endpointUri);
    }

    public RequestMatcher getRequestMatcher() {
        return this.requestMatcher;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!this.requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            sendSuccessResponse(response, this.loginMethodService.listAll());
        } catch (RuntimeException ex) {
            logger.debug("Login method list assembly failed: {}", ex.getMessage());
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR,
                    ERROR_SERVER_ERROR, ex.getMessage());
        }
    }

    private void sendSuccessResponse(HttpServletResponse response, List<LoginMethod> methods) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        List<Map<String, Object>> items = methods.stream()
                .map(LoginMethodsEndpointFilter::describe)
                .toList();

        response.getWriter().write(JacksonUtils.writeValueAsString(items));
    }

    private static Map<String, Object> describe(LoginMethod method) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", method.getName());
        item.put("type", method.getType());
        item.put("primary", method.isPrimary());
        item.put("attributes", method.getAttributes());
        return item;
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status,
                                   String error, String description) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Map<String, Object> body = new HashMap<>();
        body.put("error", error);
        if (description != null) {
            body.put("error_description", description);
        }

        response.getWriter().write(JacksonUtils.writeValueAsString(body));
    }
}
