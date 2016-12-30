/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015-2016 cFrost.sun(孙宾, SUN BIN) 
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
 * https://github.com/euler-form/web-form
 * http://eulerframework.net
 * http://cfrost.net
 */
package net.eulerframework.bootstrap;

import javax.servlet.FilterRegistration;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import net.eulerframework.web.config.MultiPartConfig;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.filter.CrosFilter;
import net.eulerframework.web.core.filter.EulerFrameworkCoreFilter;
import net.eulerframework.web.core.listener.EulerFrameworkCoreListener;

@Order(0)
public class EulerFrameworkBootstrap implements WebApplicationInitializer {
    private final Logger log = LogManager.getLogger();
    
    private static final String WEB_SECURITY_LOCAL = "web-security-local";
    private static final String WEB_SECURITY_LDAP = "web-security-ldap";
    private static final String WEB_SECURITY_CAS = "web-security-cas";
    private static final String WEB_SECURITY_NONE = "web-security-none";

    private static final String REST_SECURITY_OAUTH = "rest-security-oauth";
    private static final String REST_SECURITY_BASIC = "rest-security-basic";
    private static final String REST_SECURITY_WEB = "rest-security-web";
    private static final String REST_SECURITY_NONE = "rest-security-none";    

    private static final String OAUTH_AUTHORIZATION_SERVER = "oauth-authorization-server";
    private static final String OAUTH_RESOURCE_SERVER = "oauth-resource-server";
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        log.info("Executing Euler-Framework bootstrap.");
        
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        try {
            rootContext.register(Class.forName("net.eulerframework.config.RootContextConfiguration"));
        } catch (ClassNotFoundException e) {
            rootContext.close();
            throw new ServletException(e);
        }
        
        ConfigurableEnvironment configurableEnvironment = rootContext.getEnvironment();
        
        switch(WebConfig.getWebAuthenticationType()){
        case LOCAL:
            configurableEnvironment.addActiveProfile(WEB_SECURITY_LOCAL);break;
        case LDAP:
            configurableEnvironment.addActiveProfile(WEB_SECURITY_LDAP);break;
        case CAS:
            configurableEnvironment.addActiveProfile(WEB_SECURITY_CAS);break;
        case NONE:
            configurableEnvironment.addActiveProfile(WEB_SECURITY_NONE);break; 
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
        
        container.addListener(new ContextLoaderListener(rootContext));
        container.addListener(new EulerFrameworkCoreListener());
        
        MultiPartConfig multiPartConfig = WebConfig.getMultiPartConfig();
        
        AnnotationConfigWebApplicationContext springWebDispatcherServletContext = new AnnotationConfigWebApplicationContext();
        try {
            springWebDispatcherServletContext.register(Class.forName("net.eulerframework.config.SpringWebDispatcherServletContextConfiguration"));
        } catch (ClassNotFoundException e) {
            springWebDispatcherServletContext.close();
            rootContext.close();
            throw new ServletException(e);
        }
        DispatcherServlet springWebDispatcherServlet = new DispatcherServlet(springWebDispatcherServletContext);
        ServletRegistration.Dynamic springWebDispatcher = container.addServlet("springWebDispatcherServlet", springWebDispatcherServlet);
        springWebDispatcher.setLoadOnStartup(1);
        springWebDispatcher.setMultipartConfig(new MultipartConfigElement(
                multiPartConfig.getLocation(), 
                multiPartConfig.getMaxFileSize(), 
                multiPartConfig.getMaxRequestSize(), 
                multiPartConfig.getFileSizeThreshold()));
        springWebDispatcher.addMapping("/");

        String apiRootPath = WebConfig.getApiRootPath();
        
        AnnotationConfigWebApplicationContext springRestDispatcherServletContext = new AnnotationConfigWebApplicationContext();
        try {
            springRestDispatcherServletContext.register(Class.forName("net.eulerframework.config.SpringRestDispatcherServletContextConfiguration"));
        } catch (ClassNotFoundException e) {
            springRestDispatcherServletContext.close();
            rootContext.close();
            throw new ServletException(e);
        }
        DispatcherServlet springRestDispatcherServlet = new DispatcherServlet(springRestDispatcherServletContext);
        springRestDispatcherServlet.setDispatchOptionsRequest(true);
        ServletRegistration.Dynamic springRestDispatcher = container.addServlet("springRestDispatcherServlet", springRestDispatcherServlet);
        springRestDispatcher.setLoadOnStartup(2);
        springRestDispatcher.setMultipartConfig(new MultipartConfigElement(
                multiPartConfig.getLocation(), 
                multiPartConfig.getMaxFileSize(), 
                multiPartConfig.getMaxRequestSize(), 
                multiPartConfig.getFileSizeThreshold()));        
        springRestDispatcher.addMapping(apiRootPath+"/*");
        
        FilterRegistration.Dynamic eulerframeworkCoreFilter = container.addFilter("eulerframeworkCoreFilter", new EulerFrameworkCoreFilter());
        eulerframeworkCoreFilter.addMappingForUrlPatterns(null, false, "/*");
        
        FilterRegistration.Dynamic crosFilter = container.addFilter("crosFilter", new CrosFilter());
        crosFilter.addMappingForUrlPatterns(null, false, "/oauth/check_token", "/oauth/token");
    }
}
