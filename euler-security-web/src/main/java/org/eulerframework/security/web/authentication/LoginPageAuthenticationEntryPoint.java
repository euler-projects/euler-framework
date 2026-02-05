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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.security.web.endpoint.EulerSecurityEndpoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.*;
import org.springframework.security.web.util.RedirectUrlBuilder;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LoginPageAuthenticationEntryPoint implements AuthenticationEntryPoint, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(LoginPageAuthenticationEntryPoint.class);

    private String loginPage = EulerSecurityEndpoints.USER_LOGIN_PAGE;

    private String redirectParameter = EulerSecurityEndpoints.USER_LOGIN_SUCCESS_REDIRECT_PARAMETER;

    private boolean forceHttps = false;

    private RedirectStrategy redirectStrategy;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        String redirectUrl = buildRedirectUrlToLoginPage(request, response, authException);
        this.redirectStrategy.sendRedirect(request, response, redirectUrl);
    }

    protected String buildRedirectUrlToLoginPage(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationException authException) {
        String loginForm = determineUrlToUseForThisRequest(request, response, authException);
        if (UrlUtils.isAbsoluteUrl(loginForm)) {
            return loginForm;
        }
        int serverPort = request.getServerPort();
        String scheme = request.getScheme();
        RedirectUrlBuilder urlBuilder = new RedirectUrlBuilder();
        urlBuilder.setScheme(scheme);
        urlBuilder.setServerName(request.getServerName());
        urlBuilder.setPort(serverPort);
        urlBuilder.setContextPath(request.getContextPath());
        urlBuilder.setPathInfo(loginForm);
        if (this.forceHttps && "http".equals(scheme)) {
            urlBuilder.setScheme("https");
            urlBuilder.setPort(443);
        }
        return urlBuilder.getUrl();
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

        String loginFormUrl = this.loginPage;

        if (loginFormUrl.endsWith("?")) {
            loginFormUrl += this.redirectParameter + "=" + URLEncoder.encode(redirectValueBuilder.toString(), StandardCharsets.UTF_8);
        } else if (loginFormUrl.contains("?")) {
            loginFormUrl += "&" + this.redirectParameter + "=" + URLEncoder.encode(redirectValueBuilder.toString(), StandardCharsets.UTF_8);
        } else {
            loginFormUrl += "?" + this.redirectParameter + "=" + URLEncoder.encode(redirectValueBuilder.toString(), StandardCharsets.UTF_8);
        }

        return loginFormUrl;
    }

    public void setLoginPage(String loginPage) {
        Assert.isTrue(StringUtils.hasText(loginPage) && UrlUtils.isValidRedirectUrl(loginPage),
                "loginFormUrl must be specified and must be a valid redirect URL");
        this.loginPage = loginPage;
    }

    public void setRedirectParameter(String redirectParameter) {
        this.redirectParameter = redirectParameter;
    }

    public void setForceHttps(boolean forceHttps) {
        this.forceHttps = forceHttps;
    }

    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }

    @Override
    public void afterPropertiesSet() {
        if (this.redirectStrategy == null) {
            this.redirectStrategy = new DefaultRedirectStrategy();
        }
    }
}
