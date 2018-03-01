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
        //webLanguageFilter.addMappingForUrlPatterns(null, false, WebConfig.getStaticPagesRootPath() + "/*");
        
        FilterRegistration.Dynamic adminPageRedirectFilter = container.addFilter(EulerFilters.ADMIN_PAGE_REDIRECT_FILTER, new AdminPageRedirectFilter());
        adminPageRedirectFilter.addMappingForUrlPatterns(null, false, WebConfig.getAdminRootPath());
        
//        FilterRegistration.Dynamic ajaxRedirectResponseFilter = container.addFilter(EulerFilters.AJAX_REDIRECT_RESPONSE_FILTER, new AjaxRedirectResponseFilter());
//
//        ajaxRedirectResponseFilter.addMappingForServletNames(
//                null, 
//                false, 
//                EulerServlets.WEB_AJAX_SERVLET, 
//                EulerServlets.WEB_ADMIN_AJAX_SERVLET);
    }
}
