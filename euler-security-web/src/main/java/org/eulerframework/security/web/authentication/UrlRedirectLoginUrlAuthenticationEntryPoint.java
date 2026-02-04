/*
 * Copyright 2013-2026 the original author or authors.
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

package org.eulerframework.security.web.authentication;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlRedirectLoginUrlAuthenticationEntryPoint implements AuthenticationEntryPoint, InitializingBean {
    private final String loginFormUrl;
    private String redirectParameter = "continue";

    public UrlRedirectLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        this.loginFormUrl = loginFormUrl;
    }

    public void setRedirectParameter(String redirectParameter) {
        this.redirectParameter = redirectParameter;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        String loginForm = determineUrlToUseForThisRequest(request, response, authException);
        RequestDispatcher dispatcher = request.getRequestDispatcher(loginForm);
        dispatcher.forward(request, response);
    }

    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response,
                                                     AuthenticationException exception) {
        StringBuilder redirectValueBuilder = new StringBuilder();
        String contextPath = request.getContextPath();
        if (contextPath != null) {
            redirectValueBuilder.append(contextPath);
        }
        String servletPath = request.getServletPath();
        if (servletPath != null) {
            redirectValueBuilder.append(servletPath);
        }
        String path = request.getPathInfo();
        if (path != null) {
            redirectValueBuilder.append(path);
        }
        String queryString = request.getQueryString();
        if (queryString != null) {
            redirectValueBuilder.append("?").append(queryString);
        }

        String loginFormUrl = this.loginFormUrl;

        if (loginFormUrl.endsWith("?")) {
            loginFormUrl += this.redirectParameter + "=" + URLEncoder.encode(redirectValueBuilder.toString(), StandardCharsets.UTF_8);
        } else if (loginFormUrl.contains("?")) {
            loginFormUrl += "&" + this.redirectParameter + "=" + URLEncoder.encode(redirectValueBuilder.toString(), StandardCharsets.UTF_8);
        } else {
            loginFormUrl += "?" + this.redirectParameter + "=" + URLEncoder.encode(redirectValueBuilder.toString(), StandardCharsets.UTF_8);
        }

        return loginFormUrl;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.isTrue(StringUtils.hasText(this.loginFormUrl) && UrlUtils.isValidRedirectUrl(this.loginFormUrl),
                "loginFormUrl must be specified and must be a valid redirect URL");
    }
}
