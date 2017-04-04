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
package net.eulerframework.bootstrap;

import javax.servlet.FilterRegistration;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.config.MultiPartConfig;
import net.eulerframework.web.config.SystemProperties;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.filter.CrosFilter;
import net.eulerframework.web.core.filter.LocaleFilter;
import net.eulerframework.web.core.filter.RequestIdFilter;
import net.eulerframework.web.core.listener.EulerFrameworkCoreListener;

@Order(0)
public class EulerFrameworkBootstrap extends LogSupport implements WebApplicationInitializer {
    
    private static final String WEB_SECURITY_LOCAL = "web-security-local";
    private static final String WEB_SECURITY_LDAP = "web-security-ldap";
    private static final String WEB_SECURITY_CAS = "web-security-cas";

    private static final String REST_SECURITY_OAUTH = "rest-security-oauth";
    private static final String REST_SECURITY_BASIC = "rest-security-basic";
    private static final String REST_SECURITY_WEB = "rest-security-web";
    private static final String REST_SECURITY_NONE = "rest-security-none";    

    private static final String OAUTH_AUTHORIZATION_SERVER = "oauth-authorization-server";
    private static final String OAUTH_RESOURCE_SERVER = "oauth-resource-server";
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
        //container.addListener(new RequestContextListener());
        container.addListener(new EulerFrameworkCoreListener());
        
        MultiPartConfig multiPartConfig = WebConfig.getMultiPartConfig();
        
        this.configWebDispatcher(rootContext, container, multiPartConfig);
        
        this.configAdminDispatcher(rootContext, container, multiPartConfig);
        
        this.configApiDispatcher(rootContext, container, multiPartConfig);
        
        this.initBaseData(container);
        
        FilterRegistration.Dynamic requestIdFilter = container.addFilter("requestIdFilter", new RequestIdFilter());
        requestIdFilter.addMappingForUrlPatterns(null, false, "/*");
        
        FilterRegistration.Dynamic localeFilter = container.addFilter("localeFilter", new LocaleFilter());
        localeFilter.addMappingForServletNames(null, false, "springWebDispatcherServlet", "springAdminWebDispatcherServlet");
        
        FilterRegistration.Dynamic crosFilter = container.addFilter("crosFilter", new CrosFilter());
        crosFilter.addMappingForUrlPatterns(null, false, "/oauth/check_token", "/oauth/token");
    }
    
    private void setConfigurableEnvironment(AbstractApplicationContext rootContext) {
        ConfigurableEnvironment configurableEnvironment = rootContext.getEnvironment();
        
        switch(WebConfig.getWebAuthenticationType()){
        case LOCAL:
            configurableEnvironment.addActiveProfile(WEB_SECURITY_LOCAL);break;
        case LDAP:
            configurableEnvironment.addActiveProfile(WEB_SECURITY_LDAP);break;
        case CAS:
            configurableEnvironment.addActiveProfile(WEB_SECURITY_CAS);break;
        }
        
        switch(WebConfig.getApiAuthenticationType()){
        case OAUTH:
            configurableEnvironment.addActiveProfile(REST_SECURITY_OAUTH);break;
        case BASIC:
            configurableEnvironment.addActiveProfile(REST_SECURITY_BASIC);break;
        case WEB:
            configurableEnvironment.addActiveProfile(REST_SECURITY_WEB);break;
        case NONE:
            configurableEnvironment.addActiveProfile(REST_SECURITY_NONE);break; 
        }
        
        switch(WebConfig.getOAuthSeverType()){
        case AUTHORIZATION_SERVER:
            configurableEnvironment.addActiveProfile(OAUTH_AUTHORIZATION_SERVER);break;
        case RESOURCE_SERVER:
            configurableEnvironment.addActiveProfile(OAUTH_RESOURCE_SERVER);break;
        case BOTH:
            configurableEnvironment.addActiveProfile(OAUTH_AUTHORIZATION_SERVER);
            configurableEnvironment.addActiveProfile(OAUTH_RESOURCE_SERVER);
            break;
        case NEITHER:
            break; 
        }
    }
    
    private void configWebDispatcher(AbstractApplicationContext rootContext, ServletContext container, MultiPartConfig multiPartConfig) throws ServletException {
        AnnotationConfigWebApplicationContext springWebDispatcherServletContext = new AnnotationConfigWebApplicationContext();
        try {
            springWebDispatcherServletContext.register(Class.forName(WebConfig.getWebConfigClassName()));
        } catch (ClassNotFoundException e) {
            springWebDispatcherServletContext.close();
            rootContext.close();
            throw new ServletException(e);
        }
        DispatcherServlet springWebDispatcherServlet = new DispatcherServlet(springWebDispatcherServletContext);
        ServletRegistration.Dynamic springWebDispatcher = container.addServlet("springWebDispatcherServlet", springWebDispatcherServlet);
        springWebDispatcher.setLoadOnStartup(2);
        springWebDispatcher.setMultipartConfig(new MultipartConfigElement(
                multiPartConfig.getLocation(), 
                multiPartConfig.getMaxFileSize(), 
                multiPartConfig.getMaxRequestSize(), 
                multiPartConfig.getFileSizeThreshold()));
        springWebDispatcher.addMapping("/");
        
        this.logger.info("init web root: /");
        
    }
    
    private void configAdminDispatcher(AbstractApplicationContext rootContext, ServletContext container, MultiPartConfig multiPartConfig) throws ServletException {
        String adminRootPath = WebConfig.getAdminRootPath();
        
        AnnotationConfigWebApplicationContext springAdminWebDispatcherServletContext = new AnnotationConfigWebApplicationContext();
        try {
            springAdminWebDispatcherServletContext.register(Class.forName(WebConfig.getAdminWebConfigClassName()));
        } catch (ClassNotFoundException e) {
            springAdminWebDispatcherServletContext.close();
            rootContext.close();
            throw new ServletException(e);
        }
        DispatcherServlet springAdminWebDispatcherServlet = new DispatcherServlet(springAdminWebDispatcherServletContext);
        ServletRegistration.Dynamic springAdminWebDispatcher = container.addServlet("springAdminWebDispatcherServlet", springAdminWebDispatcherServlet);
        springAdminWebDispatcher.setLoadOnStartup(1);
        springAdminWebDispatcher.setMultipartConfig(new MultipartConfigElement(
                multiPartConfig.getLocation(), 
                multiPartConfig.getMaxFileSize(), 
                multiPartConfig.getMaxRequestSize(), 
                multiPartConfig.getFileSizeThreshold()));
        springAdminWebDispatcher.addMapping(adminRootPath+"/*");

        this.logger.info("init admin web root: " + adminRootPath+"/*");
        
    }
    
    private void configApiDispatcher(AbstractApplicationContext rootContext, ServletContext container, MultiPartConfig multiPartConfig) throws ServletException {
        String apiRootPath = WebConfig.getApiRootPath();
        
        AnnotationConfigWebApplicationContext springApiDispatcherServletContext = new AnnotationConfigWebApplicationContext();
        try {
            springApiDispatcherServletContext.register(Class.forName(WebConfig.getApiConfigClassName()));
        } catch (ClassNotFoundException e) {
            springApiDispatcherServletContext.close();
            rootContext.close();
            throw new ServletException(e);
        }
        DispatcherServlet springApiDispatcherServlet = new DispatcherServlet(springApiDispatcherServletContext);
        springApiDispatcherServlet.setDispatchOptionsRequest(true);
        ServletRegistration.Dynamic springApiDispatcher = container.addServlet("springApiDispatcherServlet", springApiDispatcherServlet);
        springApiDispatcher.setLoadOnStartup(2);
        springApiDispatcher.setMultipartConfig(new MultipartConfigElement(
                multiPartConfig.getLocation(), 
                multiPartConfig.getMaxFileSize(), 
                multiPartConfig.getMaxRequestSize(), 
                multiPartConfig.getFileSizeThreshold()));        
        springApiDispatcher.addMapping(apiRootPath+"/*");

        this.logger.info("init api root: " + apiRootPath+"/*");
        
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
