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
import org.eulerframework.web.core.base.controller.ApiSupportWebController;
import org.eulerframework.web.core.exception.web.api.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class EulerSecuritySignupAjaxController extends ApiSupportWebController implements EulerSecuritySignupEndpoint {
    private boolean signupEnabled;
    private EulerUserDetailsManager eulerUserDetailsManager;
    private PasswordEncoder passwordEncoder;

    @Override
    @PostMapping("${" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL_PROP_NAME + ":" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL + "}")
    public String doSignup(@RequestParam String username, @RequestParam String password) {
        if (!this.signupEnabled) {
            throw  new ResourceNotFoundException();
        }

        EulerUserDetails userDetails = EulerUserDetails.builder()
                .passwordEncoder(this.passwordEncoder::encode)
                .username(username)
                .password(password)
                .authorities(EulerAuthority.USER)
                .build();
        this.eulerUserDetailsManager.createUser(userDetails);
        return null;
    }

    @Value("${" + EulerSecurityEndpoints.SIGNUP_ENABLED_PROP_NAME + ":" + EulerSecurityEndpoints.SIGNUP_ENABLED + "}")
    public void setSignupEnabled(boolean signupEnabled) {
        this.signupEnabled = signupEnabled;
    }

    @Autowired
    public void setEulerUserDetailsManager(EulerUserDetailsManager eulerUserDetailsManager) {
        this.eulerUserDetailsManager = eulerUserDetailsManager;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String signupPage() {
        throw new ResourceNotFoundException();
    }
}
