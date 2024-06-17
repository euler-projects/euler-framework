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
package org.eulerframework.security.web.endpoint.csrf;

import org.eulerframework.security.web.endpoint.EulerSecurityEndpoints;
import org.eulerframework.web.core.base.controller.ThymeleafPageController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EulerSecurityXmlCsrfTokenController extends ThymeleafPageController implements EulerSecurityCsrfTokenEndpoint {
    private boolean csrfEnabled;
    @Override
    @GetMapping("${" + EulerSecurityEndpoints.CSRF_PATH_PROP_NAME + ":" + EulerSecurityEndpoints.CSRF_PATH + "}")
    public String csrf() {
        if(!this.csrfEnabled) {
            return this.notfound();
        }
        return this.display("euler/security/csrf", false);
    }

    @Value("${" + EulerSecurityEndpoints.CSRF_ENABLED_PROP_NAME + ":" + EulerSecurityEndpoints.CSRF_ENABLED + "}")
    public void setCsrfEnabled(boolean csrfEnabled) {
        this.csrfEnabled = csrfEnabled;
    }
}
