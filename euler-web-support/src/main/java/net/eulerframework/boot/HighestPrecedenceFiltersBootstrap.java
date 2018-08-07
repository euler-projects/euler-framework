/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.eulerframework.boot;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.constant.EulerFilters;
import net.eulerframework.constant.EulerServlets;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.filter.AdminPageRedirectFilter;
import net.eulerframework.web.core.filter.RequestIdFilter;
import net.eulerframework.web.core.filter.WebLanguageFilter;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class HighestPrecedenceFiltersBootstrap extends LogSupport implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        logger.info("Executing character encoding filter bootstrap.");

        FilterRegistration.Dynamic characterEncodingFilter = container.addFilter("characterEncodingFilter", new CharacterEncodingFilter("UTF-8"));
        characterEncodingFilter.addMappingForUrlPatterns(null, false, "/*");

        FilterRegistration.Dynamic requestIdFilter = container.addFilter("requestIdFilter", new RequestIdFilter());
        requestIdFilter.addMappingForUrlPatterns(null, false, "/*");

        FilterRegistration.Dynamic webLanguageFilter = container.addFilter(EulerFilters.WEB_LANGUAGE_FILTER, new WebLanguageFilter());
        
        EnumSet<DispatcherType> webLanguageFilterDispatcherType = EnumSet.of(
                DispatcherType.ERROR,
                //DispatcherType.ASYNC,
                //DispatcherType.FORWARD,
                //DispatcherType.INCLUDE,
                DispatcherType.REQUEST);
        
        webLanguageFilter.addMappingForServletNames(
                webLanguageFilterDispatcherType, 
                false, 
                EulerServlets.WEB_SERVLET, 
                EulerServlets.WEB_ADMIN_SERVLET, 
                EulerServlets.WEB_AJAX_SERVLET, 
                EulerServlets.WEB_ADMIN_AJAX_SERVLET);
        
        FilterRegistration.Dynamic adminPageRedirectFilter = container.addFilter(EulerFilters.ADMIN_PAGE_REDIRECT_FILTER, new AdminPageRedirectFilter());
        adminPageRedirectFilter.addMappingForUrlPatterns(null, false, WebConfig.getAdminRootPath());
        
        DelegatingFilterProxy corsFilterProxy = new DelegatingFilterProxy();
        corsFilterProxy.setTargetBeanName("corsFilter");;
        corsFilterProxy.setTargetFilterLifecycle(true);
        FilterRegistration.Dynamic corsFilter = container.addFilter(EulerFilters.CORS_FILTER, corsFilterProxy);
        corsFilter.addMappingForUrlPatterns(
                null, 
                false, 
                "/ajax/*", 
                WebConfig.getAdminRootPath() + "/ajax/*",
                WebConfig.getApiRootPath() + "/*");
    }
}
