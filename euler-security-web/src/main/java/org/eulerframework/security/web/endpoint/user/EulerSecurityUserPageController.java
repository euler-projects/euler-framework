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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class EulerSecurityUserPageController extends PageSupportWebController implements EulerSecurityUserEndpoint {
    private String loginProcessingUrl;
    private String logoutProcessingUrl;
    private String loginSuccessRedirectParameter;
    private String loginPageUrl;
    private String loginMethodParameter;
    private String loginMethodProcessingUrl;
    private String otpIssueEndpointUri;
    private int otpLength;

    private final LoginMethodService loginMethodService;

    /**
     * The built-in login page's display list; empty unless one is set,
     * which means the page offers everything the service serves.
     */
    private List<String> loginPageMethods = Collections.emptyList();

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
     * How many digits the OTP module issues, so the login page renders
     * that many code slots instead of guessing a length that a deployment
     * may have changed.
     */
    @ModelAttribute("otpLength")
    public int getOtpLength() {
        return otpLength;
    }

    /**
     * Splits the login methods the built-in page offers into the primary
     * (expanded) and secondary (button) groups the login template renders.
     *
     * <p>Split rule: if the request carries {@code _m=<name>}, only
     * that method is rendered as primary (enabling the user to switch
     * expanded method via GET); otherwise all methods declaring
     * {@code primary=true} are primary. When no method declares
     * primary, the first entry is promoted.
     */
    @ModelAttribute("primaryLoginMethods")
    public List<LoginMethod> getPrimaryLoginMethods(HttpServletRequest request) {
        return splitPrimary(displayedLoginMethods(), request.getParameter(this.loginMethodParameter));
    }

    @ModelAttribute("secondaryLoginMethods")
    public List<LoginMethod> getSecondaryLoginMethods(HttpServletRequest request) {
        return splitSecondary(displayedLoginMethods(), request.getParameter(this.loginMethodParameter));
    }

    /**
     * The login methods the built-in login page offers, in the order it
     * offers them: the configured display list when one is set, otherwise
     * everything the service offers, in the service's own order.
     *
     * <p>The list is a presentation concern of this page alone. The
     * service keeps offering the full, unordered set, and a method left
     * off the list stays fully functional: it remains addressable through
     * the login-method dispatch endpoint and renderable by any login page
     * of the deployment's own. Selecting such a method with {@code _m=}
     * simply finds it absent from this page's groups.
     *
     * @throws IllegalStateException if the list names a method the service
     *                               does not offer, or names one twice -
     *                               both are configuration errors this page
     *                               refuses to guess its way around
     */
    private List<LoginMethod> displayedLoginMethods() {
        List<LoginMethod> offered = this.loginMethodService.listAll();
        if (this.loginPageMethods.isEmpty()) {
            return offered;
        }
        Map<String, LoginMethod> byName = new LinkedHashMap<>();
        offered.forEach(method -> byName.put(method.getName(), method));

        List<String> unknown = this.loginPageMethods.stream()
                .filter(name -> !byName.containsKey(name))
                .distinct()
                .toList();
        if (!unknown.isEmpty()) {
            throw new IllegalStateException("Property "
                    + EulerSecurityEndpoints.USER_LOGIN_PAGE_METHODS_PROP_NAME
                    + " names login method(s) " + unknown + ", which no login method is declared under; "
                    + "the declared methods are " + byName.keySet() + ".");
        }
        Set<String> seen = new HashSet<>();
        List<String> repeated = this.loginPageMethods.stream()
                .filter(name -> !seen.add(name))
                .distinct()
                .toList();
        if (!repeated.isEmpty()) {
            throw new IllegalStateException("Property "
                    + EulerSecurityEndpoints.USER_LOGIN_PAGE_METHODS_PROP_NAME
                    + " names login method(s) " + repeated + " more than once; "
                    + "a method appears on the login page exactly where the list puts it.");
        }
        return this.loginPageMethods.stream().map(byName::get).toList();
    }

    /**
     * Sets which login methods the built-in page offers and in which
     * order. The Boot autoconfiguration calls this with the list it bound
     * from {@code euler.security.web.endpoint.user.login-page-methods};
     * a deployment assembling this controller itself calls it directly.
     *
     * <p>Values are the names clients address the methods by: the
     * declaration key beneath
     * {@code euler.security.login-method.<method-type>}, unless a
     * declaration overrides it with {@code method-name}. An empty or
     * {@code null} list - the default - offers everything the service
     * serves, in the service's own order.
     */
    public void setLoginPageMethods(List<String> loginPageMethods) {
        this.loginPageMethods = loginPageMethods == null
                ? Collections.emptyList()
                : List.copyOf(loginPageMethods);
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

    @Value("${" + EulerSecurityEndpoints.LOGIN_METHODS_DISPATCH_METHOD_PARAMETER_PROP_NAME + ":" + EulerSecurityEndpoints.LOGIN_METHODS_DISPATCH_METHOD_PARAMETER + "}")
    public void setLoginMethodParameter(String loginMethodParameter) {
        this.loginMethodParameter = loginMethodParameter;
    }

    @Value("${" + EulerSecurityEndpoints.LOGIN_METHODS_DISPATCH_PROCESSING_URL_PROP_NAME + ":" + EulerSecurityEndpoints.LOGIN_METHODS_DISPATCH_PROCESSING_URL + "}")
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

    /**
     * Bound to the same key the OTP policy reads. The default repeats the
     * one the OTP module ships with, which lives in the Boot properties
     * rather than here, so the two must be changed together.
     *
     * <p>An application that resolves its OTP policy through its own
     * resolver rather than configuration can issue a different length;
     * such an application owns its OTP policy and is expected to override
     * this reference page along with it.
     */
    @Value("${euler.security.authentication.otp.policy.otp-length:6}")
    public void setOtpLength(int otpLength) {
        this.otpLength = otpLength;
    }


}
