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
 */
package net.eulerframework.boot;

import javax.servlet.FilterRegistration;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.DispatcherServlet;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.constant.EulerFilters;
import net.eulerframework.constant.EulerServlets;
import net.eulerframework.constant.EulerSysAttributes;
import net.eulerframework.web.config.MultiPartConfig;
import net.eulerframework.web.config.SystemProperties;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.cookie.LocaleCookies;
import net.eulerframework.web.core.listener.EulerFrameworkCoreListener;

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
                rootContext.register(Class.forName(WebConfig.getRootContextConfigClassName()));
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
//            Class.forName("net.eulerframework.web.module.authentication.boot.SecurityBootstrap");
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
                WebConfig.getWebConfigClassName(),
                "/");
        
        this.initSpringMVCDispatcher(container, 
                EulerServlets.WEB_ADMIN_SERVLET, 
                1,
                WebConfig.getAdminWebConfigClassName(), 
                WebConfig.getAdminRootPath() + "/*");
        
        this.initSpringMVCDispatcher(
                container, 
                EulerServlets.WEB_AJAX_SERVLET, 
                1, 
                WebConfig.getAjaxConfigClassName(),
                "/ajax/*");
        
        this.initSpringMVCDispatcher(container, 
                EulerServlets.WEB_ADMIN_AJAX_SERVLET, 
                1,
                WebConfig.getAdminAjaxConfigClassName(), 
                WebConfig.getAdminRootPath() + "/ajax/*");

        this.initBaseData(container);

        if(WebConfig.isApiEnabled()) {
            this.initSpringMVCDispatcher(container, 
                    EulerServlets.API_SERVLET, 
                    1, 
                    WebConfig.getApiConfigClassName(),
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

        container.setAttribute(EulerSysAttributes.SITENAME.value(), WebConfig.getSitename());
        container.setAttribute(EulerSysAttributes.COPYRIGHT_HOLDER.value(), WebConfig.getCopyrightHolder());

        container.setAttribute(EulerSysAttributes.ADMIN_DASHBOARD_BRAND_ICON.value(), contextPath + WebConfig.getAdminDashboardBrandIcon());
        container.setAttribute(EulerSysAttributes.ADMIN_DASHBOARD_BRAND_TEXT.value(), WebConfig.getAdminDashboardBrandText());

        container.setAttribute(EulerSysAttributes.FRAMEWORK_VERSION.value(), SystemProperties.frameworkVersion());

        container.setAttribute(EulerSysAttributes.LOCALE_COOKIE_NAME.value(), LocaleCookies.LOCALE.getCookieName());
    }
}
