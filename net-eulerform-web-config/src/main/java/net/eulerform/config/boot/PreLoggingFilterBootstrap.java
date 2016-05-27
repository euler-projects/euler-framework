package net.eulerform.config.boot;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import net.eulerform.web.core.filter.PreLoggingFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

@Order(100)
public class PreLoggingFilterBootstrap implements WebApplicationInitializer {
    private static final Logger log = LogManager.getLogger();

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        log.info("Executing pre logging filter bootstrap.");

        FilterRegistration.Dynamic preLoggingFilter = container.addFilter("preLoggingFilter", new PreLoggingFilter());
        preLoggingFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}
