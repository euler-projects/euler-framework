package net.eulerframework.bootstrap;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.filter.CharacterEncodingFilter;

import net.eulerframework.common.base.log.LogSupport;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class CharacterEncodingFilterBootstrap extends LogSupport implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        logger.info("Executing character encoding filter bootstrap.");

        FilterRegistration.Dynamic characterEncodingFilter = container.addFilter("characterEncodingFilter", new CharacterEncodingFilter("UTF-8"));
        characterEncodingFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}
