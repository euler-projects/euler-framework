/*
 * Copyright 2013-2018 the original author or authors.
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
package net.eulerframework.web.module.authentication.extend;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.base.response.RedirectResponse;

/**
 * @author cFrost
 *
 */
public class EulerLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private ObjectMapper objectMapper;

    /**
     * @param loginFormUrl
     */
    public EulerLoginUrlAuthenticationEntryPoint(String loginFormUrl, ObjectMapper objectMapper) {
        super(loginFormUrl);
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        if (this.isAjaxRequest(request)) {
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.getOutputStream().print(this.objectMapper.writeValueAsString(
                    new RedirectResponse(this.buildRedirectUrlToLoginPage(request, response, authException))));

        } else {
            super.commence(request, response, authException);
        }
    }

    protected boolean isAjaxRequest(HttpServletRequest request) {
        return (request.getRequestURI().startsWith(request.getContextPath() + "/ajax") || request.getRequestURI()
                .startsWith(request.getContextPath() + WebConfig.getAdminRootPath() + "/ajax"));
    }
}
