/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015-2017 cFrost.sun(孙宾, SUN BIN) 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following website
 * 
 * https://eulerproject.io/euler-framework
 * https://cfrost.net
 */
package net.eulerframework.boot;

import javax.servlet.FilterRegistration;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import net.eulerframework.EulerFilters;
import net.eulerframework.EulerServlets;
import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.config.MultiPartConfig;
import net.eulerframework.web.config.SystemProperties;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.filter.CrosFilter;
import net.eulerframework.web.core.filter.WebLanguageFilter;
import net.eulerframework.web.core.listener.EulerFrameworkCoreListener;

@Order(0)
public class EulerFrameworkBootstrap extends LogSupport implements WebApplicationInitializer {

//    private static final String WEB_SECURITY_LOCAL = "web-security-local";
//    private static final String WEB_SECURITY_LDAP = "web-security-ldap";
//    private static final String WEB_SECURITY_CAS = "web-security-cas";
//
//    private static final String REST_SECURITY_OAUTH = "rest-security-oauth";
//    private static final String REST_SECURITY_BASIC = "rest-security-basic";
//    private static final String REST_SECURITY_WEB = "rest-security-web";
//    private static final String REST_SECURITY_NONE = "rest-security-none";
//
//    private static final String OAUTH_AUTHORIZATION_SERVER = "oauth-authorization-server";
//    private static final String OAUTH_RESOURCE_SERVER = "oauth-resource-server";

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        this.logger.info("Executing Euler-Framework bootstrap.");

        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        try {
            rootContext.register(Class.forName(WebConfig.getRootContextConfigClassName()));
        } catch (ClassNotFoundException e) {
            rootContext.close();
            throw new ServletException(e);
        }

        this.setConfigurableEnvironment(rootContext);

        container.addListener(new ContextLoaderListener(rootContext));
        // container.addListener(new RequestContextListener());
        container.addListener(new EulerFrameworkCoreListener());

        this.initSpringMVCDispatcher(
                container, 
                EulerServlets.WEB_SERVLET, 
                1, 
                WebConfig.getWebConfigClassName(),
                "/");
        
        this.initSpringMVCDispatcher(container, 
                EulerServlets.WEB_ADMIN_SERVLET, 
                1,
                WebConfig.getAdminWebConfigClassName(), 
                WebConfig.getAdminRootPath() + "/*");

        this.initBaseData(container);

        FilterRegistration.Dynamic webLanguageFilter = container.addFilter(EulerFilters.WEB_LANGUAGE_FILTER, new WebLanguageFilter());
        webLanguageFilter.addMappingForServletNames(null, false, EulerServlets.WEB_SERVLET, EulerServlets.WEB_ADMIN_SERVLET);

        if(WebConfig.isApiEnabled()) {
            this.initSpringMVCDispatcher(container, 
                    EulerServlets.API_SERVLET, 
                    1, 
                    WebConfig.getApiConfigClassName(),
                    WebConfig.getApiRootPath() + "/*");
            
            FilterRegistration.Dynamic crosFilter = container.addFilter(EulerFilters.CROS_FILTER, new CrosFilter());
            crosFilter.addMappingForServletNames(null, false, EulerServlets.API_SERVLET);
        }
    }

    private void setConfigurableEnvironment(AbstractApplicationContext rootContext) {
//        ConfigurableEnvironment configurableEnvironment = rootContext.getEnvironment();
//
//        switch (WebConfig.getWebAuthenticationType()) {
//        case LOCAL:
//            configurableEnvironment.addActiveProfile(WEB_SECURITY_LOCAL);
//            break;
//        case LDAP:
//            configurableEnvironment.addActiveProfile(WEB_SECURITY_LDAP);
//            break;
//        case CAS:
//            configurableEnvironment.addActiveProfile(WEB_SECURITY_CAS);
//            break;
//        }
//
//        switch (WebConfig.getApiAuthenticationType()) {
//        case OAUTH:
//            configurableEnvironment.addActiveProfile(REST_SECURITY_OAUTH);
//            break;
//        case BASIC:
//            configurableEnvironment.addActiveProfile(REST_SECURITY_BASIC);
//            break;
//        case WEB:
//            configurableEnvironment.addActiveProfile(REST_SECURITY_WEB);
//            break;
//        case NONE:
//            configurableEnvironment.addActiveProfile(REST_SECURITY_NONE);
//            break;
//        }
//
//        switch (WebConfig.getOAuthSeverType()) {
//        case AUTHORIZATION_SERVER:
//            configurableEnvironment.addActiveProfile(OAUTH_AUTHORIZATION_SERVER);
//            break;
//        case RESOURCE_SERVER:
//            configurableEnvironment.addActiveProfile(OAUTH_RESOURCE_SERVER);
//            break;
//        case BOTH:
//            configurableEnvironment.addActiveProfile(OAUTH_AUTHORIZATION_SERVER);
//            configurableEnvironment.addActiveProfile(OAUTH_RESOURCE_SERVER);
//            break;
//        case NEITHER:
//            break;
//        }
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

        container.setAttribute("__CONTEXT_PATH", contextPath);
        container.setAttribute("__ASSETS_PATH", contextPath + WebConfig.getAssetsPath());

        container.setAttribute("__FILE_DOWNLOAD_PATH", contextPath + "/file");
        container.setAttribute("__FILE_UPLOAD_ACTION", contextPath + "/uploadFile");

        container.setAttribute("__DEBUG_MODE", WebConfig.isDebugMode());
        container.setAttribute("__PROJECT_VERSION", WebConfig.getProjectVersion());
        container.setAttribute("__PROJECT_MODE", WebConfig.getProjectMode());
        container.setAttribute("__PROJECT_BUILDTIME", WebConfig.getProjectBuildtime());

        container.setAttribute("__SITENAME", WebConfig.getSitename());
        container.setAttribute("__COPYRIGHT_HOLDER", WebConfig.getCopyrightHolder());

        container.setAttribute("__ADMIN_DASHBOARD_BRAND_ICON", contextPath + WebConfig.getAdminDashboardBrandIcon());
        container.setAttribute("__ADMIN_DASHBOARD_BRAND_TEXT", WebConfig.getAdminDashboardBrandText());

        container.setAttribute("__FRAMEWORK_VERSION", SystemProperties.frameworkVersion());
    }
}
