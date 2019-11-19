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
package org.eulerframework.boot;

import javax.servlet.FilterRegistration;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.eulerframework.config.EulerWebSupportConfig;
import org.eulerframework.web.config.MultiPartConfig;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.DispatcherServlet;

import org.eulerframework.common.base.log.LogSupport;
import org.eulerframework.constant.EulerFilters;
import org.eulerframework.constant.EulerServlets;
import org.eulerframework.constant.EulerSysAttributes;
import org.eulerframework.web.config.SystemProperties;
import org.eulerframework.web.config.WebConfig;
import org.eulerframework.web.core.cookie.LocaleCookies;
import org.eulerframework.web.core.listener.EulerFrameworkCoreListener;

@Order(0)
public class EulerFrameworkBootstrap extends LogSupport implements WebApplicationInitializer {
    
    /*
     * Spring Security的启动类将此参数放入ServletContext，以标识Spring Security已启用，此处跳过ContextLoaderListener的初始化，
     * 更好的办法：判断是否存在AbstractSecurityWebApplicationInitializer的实现类
     */
    public final static String EULER_SPRING_SECURITY_ENABLED = "__EULER_SPRING_SECURITY_ENABLED";

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        this.logger.info("Executing Euler-Framework bootstrap.");
        
        if(container.getAttribute(EULER_SPRING_SECURITY_ENABLED) == null
                || !container.getAttribute(EULER_SPRING_SECURITY_ENABLED).equals(true)) {
            AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
            try {
                rootContext.register(Class.forName(EulerWebSupportConfig.getRootContextConfigClassName()));
            } catch (ClassNotFoundException e2) {
                rootContext.close();
                throw new ServletException(e2);
            }

            container.addListener(new ContextLoaderListener(rootContext));
        } else {
            this.logger.info("Spring security was enabled, skip ContextLoaderListener init.");
        }
        
        /*
         * 判断是否存在AbstractSecurityWebApplicationInitializer的实现类, 若不存在则在此处注册ContextLoaderListener
         */
//        try {
//            Class.forName("org.eulerframework.web.module.authentication.boot.SecurityBootstrap");
//        } catch (ClassNotFoundException e) {
//            AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
//            try {
//                rootContext.register(Class.forName(WebConfig.getRootContextConfigClassName()));
//            } catch (ClassNotFoundException e2) {
//                rootContext.close();
//                throw new ServletException(e2);
//            }
//
//            container.addListener(new ContextLoaderListener(rootContext));
//        }

        container.addListener(new EulerFrameworkCoreListener());
        
        FilterRegistration.Dynamic webLanguageFilter = container.addFilter(EulerFilters.E_TAG_FILTER, new ShallowEtagHeaderFilter());
        webLanguageFilter.addMappingForServletNames(null, false, EulerServlets.WEB_SERVLET, EulerServlets.WEB_ADMIN_SERVLET);
        
        this.initSpringMVCDispatcher(
                container, 
                EulerServlets.WEB_SERVLET, 
                1,
                EulerWebSupportConfig.getWebConfigClassName(),
                "/");
        
        this.initSpringMVCDispatcher(container, 
                EulerServlets.WEB_ADMIN_SERVLET, 
                1,
                EulerWebSupportConfig.getAdminWebConfigClassName(),
                WebConfig.getAdminRootPath() + "/*");
        
        this.initSpringMVCDispatcher(
                container, 
                EulerServlets.WEB_AJAX_SERVLET, 
                1,
                EulerWebSupportConfig.getAjaxConfigClassName(),
                "/ajax/*");
        
        this.initSpringMVCDispatcher(container, 
                EulerServlets.WEB_ADMIN_AJAX_SERVLET, 
                1,
                EulerWebSupportConfig.getAdminAjaxConfigClassName(),
                WebConfig.getAdminRootPath() + "/ajax/*");

        this.initBaseData(container);

        if(WebConfig.isApiEnabled()) {
            this.initSpringMVCDispatcher(container, 
                    EulerServlets.API_SERVLET, 
                    1,
                    EulerWebSupportConfig.getApiConfigClassName(),
                    WebConfig.getApiRootPath() + "/*");
        }
    }

    private void initSpringMVCDispatcher(
            ServletContext container, 
            String servletName, 
            int loadOnStartup,
            String configClassName, 
            String... urlPatterns) throws ServletException {
        AnnotationConfigWebApplicationContext springDispatcherServletContext = new AnnotationConfigWebApplicationContext();
        try {
            springDispatcherServletContext.register(Class.forName(configClassName));
        } catch (ClassNotFoundException e) {
            springDispatcherServletContext.close();
            throw new ServletException(e);
        }
        DispatcherServlet springDispatcherServlet = new DispatcherServlet(springDispatcherServletContext);
        springDispatcherServlet.setDispatchOptionsRequest(true);
        ServletRegistration.Dynamic springDispatcherServletDynamic = container.addServlet(servletName,
                springDispatcherServlet);
        springDispatcherServletDynamic.setLoadOnStartup(loadOnStartup);

        MultiPartConfig multiPartConfig = WebConfig.getMultiPartConfig();

        springDispatcherServletDynamic.setMultipartConfig(
                new MultipartConfigElement(multiPartConfig.getLocation(), multiPartConfig.getMaxFileSize(),
                        multiPartConfig.getMaxRequestSize(), multiPartConfig.getFileSizeThreshold()));
        springDispatcherServletDynamic.addMapping(urlPatterns);
    }

    private void initBaseData(ServletContext container) {
        String contextPath = container.getContextPath();
        container.setAttribute(EulerSysAttributes.WEB_URL.value(), WebConfig.getWebUrl());

        container.setAttribute(EulerSysAttributes.CONTEXT_PATH.value(), contextPath);
        container.setAttribute(EulerSysAttributes.ASSETS_PATH.value(), contextPath + WebConfig.getAssetsPath());
        container.setAttribute(EulerSysAttributes.ADMIN_PATH.value(), contextPath + WebConfig.getAdminRootPath());
        container.setAttribute(EulerSysAttributes.AJAX_PATH.value(), contextPath + "/ajax");
        container.setAttribute(EulerSysAttributes.ADMIN_AJAX_PATH.value(), contextPath + WebConfig.getAdminRootPath() + "/ajax");

        container.setAttribute(EulerSysAttributes.DEBUG_MODE.value(), WebConfig.isDebugMode());
        container.setAttribute(EulerSysAttributes.PROJECT_VERSION.value(), WebConfig.getProjectVersion());
        container.setAttribute(EulerSysAttributes.PROJECT_MODE.value(), WebConfig.getProjectMode());
        container.setAttribute(EulerSysAttributes.PROJECT_BUILDTIME.value(), WebConfig.getProjectBuildtime());

        container.setAttribute(EulerSysAttributes.SITENAME.value(), WebConfig.getSiteName());
        container.setAttribute(EulerSysAttributes.COPYRIGHT_HOLDER.value(), WebConfig.getCopyrightHolder());

        container.setAttribute(EulerSysAttributes.ADMIN_DASHBOARD_BRAND_ICON.value(), contextPath + WebConfig.getAdminDashboardBrandIcon());
        container.setAttribute(EulerSysAttributes.ADMIN_DASHBOARD_BRAND_TEXT.value(), WebConfig.getAdminDashboardBrandText());

        container.setAttribute(EulerSysAttributes.FRAMEWORK_VERSION.value(), SystemProperties.frameworkVersion());

        container.setAttribute(EulerSysAttributes.LOCALE_COOKIE_NAME.value(), LocaleCookies.LOCALE.getCookieName());
    }
}
