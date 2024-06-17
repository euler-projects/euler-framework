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

import org.eulerframework.security.web.endpoint.EulerSecurityEndpoints;
import org.eulerframework.web.core.base.controller.ThymeleafPageController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class EulerSecurityUserPageController extends ThymeleafPageController implements EulerSecurityUserEndpoint {
    private String loginProcessingUrl;
    private String logoutProcessingUrl;

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.USER_LOGIN_PAGE_PROP_NAME + ":" + EulerSecurityEndpoints.USER_LOGIN_PAGE + "}")
    public String loginPage() {
        return this.display("/euler/security/login");
    }

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.USER_LOGOUT_PAGE_PROP_NAME + ":" + EulerSecurityEndpoints.USER_LOGOUT_PAGE + "}")
    public String logoutPage() {
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

    @Value("${" + EulerSecurityEndpoints.USER_LOGIN_PROCESSING_URL_PROP_NAME + ":" + EulerSecurityEndpoints.USER_LOGIN_PROCESSING_URL + "}")
    public void setLoginProcessingUrl(String loginProcessingUrl) {
        this.loginProcessingUrl = loginProcessingUrl;
    }

    @Value("${" + EulerSecurityEndpoints.USER_LOGOUT_PROCESSING_URL_PROP_NAME + ":" + EulerSecurityEndpoints.USER_LOGOUT_PROCESSING_URL + "}")
    public void setLogoutProcessingUrl(String logoutProcessingUrl) {
        this.logoutProcessingUrl = logoutProcessingUrl;
    }
}
