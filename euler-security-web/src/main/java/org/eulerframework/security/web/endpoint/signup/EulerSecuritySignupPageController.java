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
package org.eulerframework.security.web.endpoint.signup;

import org.eulerframework.security.core.EulerAuthority;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.provisioning.EulerUserDetailsManager;
import org.eulerframework.security.web.endpoint.EulerSecurityEndpoints;
import org.eulerframework.web.core.base.controller.PageRender;
import org.eulerframework.web.core.base.controller.PageSupportWebController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class EulerSecuritySignupPageController extends PageSupportWebController implements EulerSecuritySignupEndpoint {
    private boolean signupEnabled;
    private String loginPage;
    private String signupProcessingUrl;
    private EulerUserDetailsManager eulerUserDetailsManager;
    private PasswordEncoder passwordEncoder;

    public EulerSecuritySignupPageController(PageRender pageRender) {
        super(pageRender);
    }

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.SIGNUP_PAGE_PROP_NAME + ":" + EulerSecurityEndpoints.SIGNUP_PAGE + "}")
    public ModelAndView signupPage() {
        if (!this.signupEnabled) {
            return this.notfound();
        }
        return this.display("/euler/security/signup");
    }

    @Override
    @PostMapping("${" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL_PROP_NAME + ":" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL + "}")
    public ModelAndView doSignup(@RequestParam String username, @RequestParam String password) {
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
        return this.success(null, new Target(this.loginPage, "_SIGN_IN"));
    }

    @ModelAttribute("signupProcessingUrl")
    public String getSignupProcessingUrl() {
        return signupProcessingUrl;
    }

    @Value("${" + EulerSecurityEndpoints.SIGNUP_ENABLED_PROP_NAME + ":" + EulerSecurityEndpoints.SIGNUP_ENABLED + "}")
    public void setSignupEnabled(boolean signupEnabled) {
        this.signupEnabled = signupEnabled;
    }

    @Value("${" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL_PROP_NAME + ":" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL + "}")
    public void setSignupProcessingUrl(String signupProcessingUrl) {
        this.signupProcessingUrl = signupProcessingUrl;
    }

    @Value("${" + EulerSecurityEndpoints.USER_LOGIN_PAGE_PROP_NAME + ":" + EulerSecurityEndpoints.USER_LOGIN_PAGE + "}")
    public void setLoginPage(String loginPage) {
        this.loginPage = loginPage;
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
