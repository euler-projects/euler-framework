package net.eulerframework.bootstrap;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.filter.CharacterEncodingFilter;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.core.filter.RequestIdFilter;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class HighestPrecedenceFiltersBootstrap extends LogSupport implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        logger.info("Executing character encoding filter bootstrap.");

        FilterRegistration.Dynamic characterEncodingFilter = container.addFilter("characterEncodingFilter", new CharacterEncodingFilter("UTF-8"));
        characterEncodingFilter.addMappingForUrlPatterns(null, false, "/*");

        FilterRegistration.Dynamic requestIdFilter = container.addFilter("requestIdFilter", new RequestIdFilter());
        requestIdFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}
