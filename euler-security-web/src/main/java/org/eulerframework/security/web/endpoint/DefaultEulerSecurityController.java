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
package org.eulerframework.security.web.endpoint;

import org.eulerframework.security.conf.SecurityConfig;
import org.eulerframework.security.core.userdetails.provisioning.EulerUserDetailsManager;
import org.eulerframework.web.core.base.controller.ThymeleafSupportWebController;
import org.eulerframework.web.util.ServletUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Controller
public class DefaultEulerSecurityController extends ThymeleafSupportWebController implements EulerSecurityController {
    private boolean signupEnabled;
    private String signupProcessingUrl;
    private String loginProcessingUrl;
    private String logoutProcessingUrl;
    private EulerUserDetailsManager eulerUserDetailsManager;
    private PasswordEncoder passwordEncoder;

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.SIGNUP_PAGE_PROPERTY_NAME + ":" + EulerSecurityEndpoints.SIGNUP_PAGE + "}")
    public String signupPage() throws NoResourceFoundException {
        if (!this.signupEnabled) {
            throw new NoResourceFoundException(HttpMethod.GET, ServletUtils.findRealURI(this.getRequest()));
        }
        return "euler/security/web/signup";
    }

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.LOGIN_PAGE_PROPERTY_NAME + ":" + EulerSecurityEndpoints.LOGIN_PAGE + "}")
    public String loginPage() {
        return "euler/security/web/login";
    }

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.LOGOUT_PAGE_PROPERTY_NAME + ":" + EulerSecurityEndpoints.LOGOUT_PAGE + "}")
    public String logoutPage() {
        return "euler/security/web/logout";
    }

    //@Override
    @GetMapping("${" + EulerSecurityEndpoints.CHANGE_PASSWORD_PAGE_PROPERTY_NAME + ":" + EulerSecurityEndpoints.CHANGE_PASSWORD_PAGE + "}")
    public String changePasswordPage() {
        return "euler/security/web/change-password";
    }

    @Override
    @PostMapping("change-password")
    @ResponseBody
    public void changePassword(String oldRawPassword, String newRawPassword) {
        this.eulerUserDetailsManager.changePassword(oldRawPassword, this.passwordEncoder.encode(newRawPassword));
    }

    @PostMapping("${" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL_PROPERTY_NAME + ":" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL + "}")
    public String litesignup(
            @RequestParam String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String mobile,
            @RequestParam String password) {
        if (SecurityConfig.isSignUpEnabled()) {
            if (SecurityConfig.isSignUpEnableCaptcha()) {
                //Captcha.validCaptcha(this.getRequest());
            }

            //this.eulerUserService.signUp(username, email, mobile, password);
            return this.success();
        } else {
            return this.notfound();
        }
    }

    @Value("${" + EulerSecurityEndpoints.SIGNUP_ENABLED_PROPERTY_NAME + ":" + EulerSecurityEndpoints.SIGNUP_ENABLED + "}")
    public void setSignupEnabled(boolean signupEnabled) {
        this.signupEnabled = signupEnabled;
    }

    @ModelAttribute("signupProcessingUrl")
    public String getSignupProcessingUrl() {
        return signupProcessingUrl;
    }

    @Value("${" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL_PROPERTY_NAME + ":" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL + "}")
    public void setSignupProcessingUrl(String signupProcessingUrl) {
        this.signupProcessingUrl = signupProcessingUrl;
    }

    @ModelAttribute("loginProcessingUrl")
    public String getLoginProcessingUrl() {
        return loginProcessingUrl;
    }

    @Value("${" + EulerSecurityEndpoints.LOGIN_PROCESSING_URL_PROPERTY_NAME + ":" + EulerSecurityEndpoints.LOGIN_PROCESSING_URL + "}")
    public void setLoginProcessingUrl(String loginProcessingUrl) {
        this.loginProcessingUrl = loginProcessingUrl;
    }

    @ModelAttribute("logoutProcessingUrl")
    public String getLogoutProcessingUrl() {
        return logoutProcessingUrl;
    }

    @Value("${" + EulerSecurityEndpoints.LOGOUT_PROCESSING_URL_PROPERTY_NAME + ":" + EulerSecurityEndpoints.LOGOUT_PROCESSING_URL + "}")
    public void setLogoutProcessingUrl(String logoutProcessingUrl) {
        this.logoutProcessingUrl = logoutProcessingUrl;
    }

    @Autowired
    public void setEulerUserDetailsManager(EulerUserDetailsManager eulerUserDetailsManager) {
        this.eulerUserDetailsManager = eulerUserDetailsManager;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
