package net.eulerframework.bootstrap;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.core.filter.PreLoggingFilter;

@Order(100)
public class PreLoggingFilterBootstrap extends LogSupport implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        logger.info("Executing pre logging filter bootstrap.");

        FilterRegistration.Dynamic preLoggingFilter = container.addFilter("preLoggingFilter", new PreLoggingFilter());
        preLoggingFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}
