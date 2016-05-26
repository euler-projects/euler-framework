package net.eulerform.config.boot;

import net.eulerform.web.core.filter.PostLoggingFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@Order(300)
public class PostLoggingBootstrap implements WebApplicationInitializer {
    private static final Logger log = LogManager.getLogger();

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        log.info("Executing post logging bootstrap.");

        FilterRegistration.Dynamic postLoggingFilter = container.addFilter("postLoggingFilter", new PostLoggingFilter());
        postLoggingFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}
