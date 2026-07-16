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
import org.eulerframework.security.web.endpoint.user.login.LoginMethodContributor;
import org.eulerframework.security.web.endpoint.user.login.LoginMethodView;
import org.eulerframework.web.core.base.controller.PageRender;
import org.eulerframework.web.core.base.controller.PageSupportWebController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
public class EulerSecurityUserPageController extends PageSupportWebController implements EulerSecurityUserEndpoint {
    private String loginProcessingUrl;
    private String logoutProcessingUrl;
    private String loginSuccessRedirectParameter;

    private ObjectProvider<LoginMethodContributor> loginMethodContributorProvider;

    public EulerSecurityUserPageController(PageRender pageRender) {
        super(pageRender);
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

    /**
     * Aggregates every {@link LoginMethodContributor} bean in the
     * application context into a single flat list, exposed to the
     * shared login template as {@code loginMethods}. The template
     * dispatches on {@link LoginMethodView#type()} to render each
     * entry.
     *
     * <p>Ordering: contributor beans are iterated in Spring
     * {@link org.springframework.core.Ordered} order (via
     * {@link ObjectProvider#orderedStream()}); the flattened list is
     * then stably sorted by {@link LoginMethodView#order()} so a
     * single contributor emitting multiple methods (e.g. several
     * OAuth2 IdPs) can control their relative position via the view's
     * own order field.
     *
     * <p>When no contributor bean is registered the model attribute
     * is an empty list, so the template can call
     * {@code loginMethods.isEmpty()} without null guards.
     */
    @ModelAttribute("loginMethods")
    public List<LoginMethodView> getLoginMethods() {
        if (this.loginMethodContributorProvider == null) {
            return Collections.emptyList();
        }
        List<LoginMethodView> aggregated = new ArrayList<>();
        this.loginMethodContributorProvider.orderedStream().forEach(contributor -> {
            List<LoginMethodView> views = contributor.contribute();
            if (views != null && !views.isEmpty()) {
                aggregated.addAll(views);
            }
        });
        if (aggregated.size() > 1) {
            aggregated.sort(Comparator.comparingInt(LoginMethodView::order));
        }
        return Collections.unmodifiableList(aggregated);
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

    @Autowired(required = false)
    public void setLoginMethodContributorProvider(
            ObjectProvider<LoginMethodContributor> loginMethodContributorProvider) {
        this.loginMethodContributorProvider = loginMethodContributorProvider;
    }
}
