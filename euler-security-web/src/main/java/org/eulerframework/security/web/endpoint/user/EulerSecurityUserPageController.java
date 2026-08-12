/*
 * Copyright 2013-2024 the original author or authors.
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
package org.eulerframework.security.web.endpoint.user;

import org.eulerframework.security.config.annotation.web.configurers.otp.OneTimePasswordLoginConfigurer;
import org.eulerframework.security.web.endpoint.EulerSecurityEndpoints;
import org.eulerframework.security.web.login.LoginMethod;
import org.eulerframework.security.web.login.LoginMethodService;
import org.eulerframework.web.core.base.controller.PageRender;
import org.eulerframework.web.core.base.controller.PageSupportWebController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class EulerSecurityUserPageController extends PageSupportWebController implements EulerSecurityUserEndpoint {
    private String loginProcessingUrl;
    private String logoutProcessingUrl;
    private String loginSuccessRedirectParameter;
    private String loginPageUrl;
    private String loginMethodParameter;
    private String loginMethodProcessingUrl;
    private String otpIssueEndpointUri;

    private final LoginMethodService loginMethodService;

    public EulerSecurityUserPageController(PageRender pageRender, LoginMethodService loginMethodService) {
        super(pageRender);
        this.loginMethodService = loginMethodService;
    }

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.USER_LOGIN_PAGE_PROP_NAME + ":" + EulerSecurityEndpoints.USER_LOGIN_PAGE + "}")
    public ModelAndView loginPage() {
        return this.display("/euler/security/login");
    }

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.USER_LOGOUT_PAGE_PROP_NAME + ":" + EulerSecurityEndpoints.USER_LOGOUT_PAGE + "}")
    public ModelAndView logoutPage() {
        return this.display("/euler/security/logout");
    }

    @ModelAttribute("loginProcessingUrl")
    public String getLoginProcessingUrl() {
        return loginProcessingUrl;
    }

    @ModelAttribute("logoutProcessingUrl")
    public String getLogoutProcessingUrl() {
        return logoutProcessingUrl;
    }

    @ModelAttribute("loginSuccessRedirectParameter")
    public String getLoginSuccessRedirectParameter() {
        return loginSuccessRedirectParameter;
    }

    @ModelAttribute("loginPageUrl")
    public String getLoginPageUrl() {
        return loginPageUrl;
    }

    @ModelAttribute("loginMethodParameter")
    public String getLoginMethodParameter() {
        return loginMethodParameter;
    }

    @ModelAttribute("loginMethodProcessingUrl")
    public String getLoginMethodProcessingUrl() {
        return loginMethodProcessingUrl;
    }

    /**
     * The OTP issue endpoint, where a client asks for an
     * {@code otp_ticket} before it can submit an OTP login. Exposed to
     * the page because obtaining a ticket is a plain API call, not a
     * login-method submission.
     */
    @ModelAttribute("otpIssueEndpointUri")
    public String getOtpIssueEndpointUri() {
        return otpIssueEndpointUri;
    }

    /**
     * Splits the offered login methods into the primary (expanded) and
     * secondary (button) groups the login template renders.
     *
     * <p>Split rule: if the request carries {@code _m=<name>}, only
     * that method is rendered as primary (enabling the user to switch
     * expanded method via GET); otherwise all methods declaring
     * {@code primary=true} are primary. When no method declares
     * primary, the first entry is promoted.
     */
    @ModelAttribute("primaryLoginMethods")
    public List<LoginMethod> getPrimaryLoginMethods(HttpServletRequest request) {
        List<LoginMethod> all = this.loginMethodService.listAll();
        String selectedMethod = request.getParameter(this.loginMethodParameter);
        return splitPrimary(all, selectedMethod);
    }

    @ModelAttribute("secondaryLoginMethods")
    public List<LoginMethod> getSecondaryLoginMethods(HttpServletRequest request) {
        List<LoginMethod> all = this.loginMethodService.listAll();
        String selectedMethod = request.getParameter(this.loginMethodParameter);
        return splitSecondary(all, selectedMethod);
    }

    private List<LoginMethod> splitPrimary(List<LoginMethod> all, String selectedMethod) {
        if (all.isEmpty()) return Collections.emptyList();
        if (selectedMethod != null && !selectedMethod.isEmpty()) {
            return all.stream()
                    .filter(v -> selectedMethod.equals(v.getName()))
                    .toList();
        }
        List<LoginMethod> declared = all.stream().filter(LoginMethod::isPrimary).toList();
        if (!declared.isEmpty()) return declared;
        // No primary declared: first entry is promoted.
        return List.of(all.get(0));
    }

    private List<LoginMethod> splitSecondary(List<LoginMethod> all, String selectedMethod) {
        if (all.isEmpty()) return Collections.emptyList();
        List<LoginMethod> primary = splitPrimary(all, selectedMethod);
        return all.stream()
                .filter(v -> !primary.contains(v))
                .toList();
    }

    @Value("${" + EulerSecurityEndpoints.USER_LOGIN_PROCESSING_URL_PROP_NAME + ":" + EulerSecurityEndpoints.USER_LOGIN_PROCESSING_URL + "}")
    public void setLoginProcessingUrl(String loginProcessingUrl) {
        this.loginProcessingUrl = loginProcessingUrl;
    }

    @Value("${" + EulerSecurityEndpoints.USER_LOGOUT_PROCESSING_URL_PROP_NAME + ":" + EulerSecurityEndpoints.USER_LOGOUT_PROCESSING_URL + "}")
    public void setLogoutProcessingUrl(String logoutProcessingUrl) {
        this.logoutProcessingUrl = logoutProcessingUrl;
    }

    @Value("${" + EulerSecurityEndpoints.USER_LOGIN_SUCCESS_REDIRECT_PARAMETER_PROP_NAME + ":" + EulerSecurityEndpoints.USER_LOGIN_SUCCESS_REDIRECT_PARAMETER + "}")
    public void setLoginSuccessRedirectParameter(String loginSuccessRedirectParameter) {
        this.loginSuccessRedirectParameter = loginSuccessRedirectParameter;
    }

    @Value("${" + EulerSecurityEndpoints.USER_LOGIN_PAGE_PROP_NAME + ":" + EulerSecurityEndpoints.USER_LOGIN_PAGE + "}")
    public void setLoginPageUrl(String loginPageUrl) {
        this.loginPageUrl = loginPageUrl;
    }

    @Value("${" + EulerSecurityEndpoints.LOGIN_METHOD_DISPATCH_METHOD_PARAMETER_PROP_NAME + ":" + EulerSecurityEndpoints.LOGIN_METHOD_DISPATCH_METHOD_PARAMETER + "}")
    public void setLoginMethodParameter(String loginMethodParameter) {
        this.loginMethodParameter = loginMethodParameter;
    }

    @Value("${" + EulerSecurityEndpoints.LOGIN_METHOD_DISPATCH_PROCESSING_URL_PROP_NAME + ":" + EulerSecurityEndpoints.LOGIN_METHOD_DISPATCH_PROCESSING_URL + "}")
    public void setLoginMethodProcessingUrl(String loginMethodProcessingUrl) {
        this.loginMethodProcessingUrl = loginMethodProcessingUrl;
    }

    /**
     * Bound to the same key the OTP module reads, so the page always
     * points at the endpoint actually serving tickets.
     */
    @Value("${euler.security.authentication.otp.issue-endpoint-uri:"
            + OneTimePasswordLoginConfigurer.DEFAULT_ISSUE_ENDPOINT_URI + "}")
    public void setOtpIssueEndpointUri(String otpIssueEndpointUri) {
        this.otpIssueEndpointUri = otpIssueEndpointUri;
    }


}
