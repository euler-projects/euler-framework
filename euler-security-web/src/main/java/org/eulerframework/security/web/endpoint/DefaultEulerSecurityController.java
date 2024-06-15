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

import org.eulerframework.security.core.EulerAuthority;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.provisioning.EulerUserDetailsManager;
import org.eulerframework.web.core.base.controller.ThymeleafPageController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Controller
public class DefaultEulerSecurityController extends ThymeleafPageController implements EulerSecurityController {
    private boolean signupEnabled;
    private String signupProcessingUrl;
    private String loginProcessingUrl;
    private String logoutProcessingUrl;
    private String changePasswordProcessingUrl;
    private EulerUserDetailsManager eulerUserDetailsManager;
    private PasswordEncoder passwordEncoder;

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.SIGNUP_PAGE_PROPERTY_NAME + ":" + EulerSecurityEndpoints.SIGNUP_PAGE + "}")
    public String signupPage() throws NoResourceFoundException {
        if (!this.signupEnabled) {
            return this.notfound();
        }
        return this.display("/euler/security/web/signup");
    }

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.LOGIN_PAGE_PROPERTY_NAME + ":" + EulerSecurityEndpoints.LOGIN_PAGE + "}")
    public String loginPage() {
        return this.display("/euler/security/web/login");
    }

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.LOGOUT_PAGE_PROPERTY_NAME + ":" + EulerSecurityEndpoints.LOGOUT_PAGE + "}")
    public String logoutPage() {
        return this.display("/euler/security/web/logout");
    }

    //@Override
    @GetMapping("${" + EulerSecurityEndpoints.CHANGE_PASSWORD_PAGE_PROPERTY_NAME + ":" + EulerSecurityEndpoints.CHANGE_PASSWORD_PAGE + "}")
    public String changePasswordPage() {
        return this.display("/euler/security/web/change-password");
    }

    @PostMapping("${" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL_PROPERTY_NAME + ":" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL + "}")
    public String signup(@RequestParam String username, @RequestParam String password) {
        if (!this.signupEnabled) {
            return this.notfound();
        }
//        if (SecurityConfig.isSignUpEnableCaptcha()) {
//            Captcha.validCaptcha(this.getRequest());
//        }

        EulerUserDetails userDetails = EulerUserDetails.builder()
                .passwordEncoder(this.passwordEncoder::encode)
                .username(username)
                .password(password)
                .authorities(EulerAuthority.USER)
                .build();
        this.eulerUserDetailsManager.createUser(userDetails);
        return this.success(null, new Target("login", "_SIGN_IN"));
    }

    @Override
    @PostMapping("${" + EulerSecurityEndpoints.CHANGE_PASSWORD_PROCESSING_URL_PROPERTY_NAME + ":" + EulerSecurityEndpoints.CHANGE_PASSWORD_PROCESSING_URL + "}")
    public String changePassword(String oldRawPassword, String newRawPassword) {
        this.eulerUserDetailsManager.changePassword(oldRawPassword, this.passwordEncoder.encode(newRawPassword));
        return this.success();
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

    @ModelAttribute("changePasswordProcessingUrl")
    public String getChangePasswordProcessingUrl() {
        return changePasswordProcessingUrl;
    }

    @Value("${" + EulerSecurityEndpoints.CHANGE_PASSWORD_PROCESSING_URL_PROPERTY_NAME + ":" + EulerSecurityEndpoints.CHANGE_PASSWORD_PROCESSING_URL + "}")
    public void setChangePasswordProcessingUrl(String changePasswordProcessingUrl) {
        this.changePasswordProcessingUrl = changePasswordProcessingUrl;
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
