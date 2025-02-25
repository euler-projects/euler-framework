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
package org.eulerframework.security.web.endpoint.password;

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
import org.springframework.web.servlet.ModelAndView;

@Controller
public class EulerSecurityPasswordPageController extends PageSupportWebController implements EulerSecurityPasswordEndpoint {
    private String changePasswordProcessingUrl;
    private EulerUserDetailsManager eulerUserDetailsManager;
    private PasswordEncoder passwordEncoder;

    public EulerSecurityPasswordPageController(PageRender pageRender) {
        super(pageRender);
    }

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.PASSWORD_CHANGE_PASSWORD_PAGE_PROP_NAME + ":" + EulerSecurityEndpoints.PASSWORD_CHANGE_PASSWORD_PAGE + "}")
    public ModelAndView changePasswordPage() {
        return this.display("/euler/security/change-password");
    }

    @Override
    @PostMapping("${" + EulerSecurityEndpoints.PASSWORD_CHANGE_PASSWORD_PROCESSING_URL_PROP_NAME + ":" + EulerSecurityEndpoints.PASSWORD_CHANGE_PASSWORD_PROCESSING_URL + "}")
    public ModelAndView doChangePassword(String oldRawPassword, String newRawPassword) {
        this.eulerUserDetailsManager.changePassword(oldRawPassword, this.passwordEncoder.encode(newRawPassword));
        return this.success();
    }

    @ModelAttribute("changePasswordProcessingUrl")
    public String getChangePasswordProcessingUrl() {
        return changePasswordProcessingUrl;
    }

    @Value("${" + EulerSecurityEndpoints.PASSWORD_CHANGE_PASSWORD_PROCESSING_URL_PROP_NAME + ":" + EulerSecurityEndpoints.PASSWORD_CHANGE_PASSWORD_PROCESSING_URL + "}")
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
