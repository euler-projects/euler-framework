package net.eulerframework.web.module.authentication.bootstrap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import net.eulerframework.web.module.authentication.filter.AuthenticateInfoFilter;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@Order(300)
public class AuthenticateBootstrap implements WebApplicationInitializer {
    private static final Logger log = LogManager.getLogger();

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        log.info("Executing authenticate logging bootstrap.");

        FilterRegistration.Dynamic authenticateInfoFilter = container.addFilter("authenticateInfoFilter", new AuthenticateInfoFilter("oauth/token","oauth/check_token"));
        authenticateInfoFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}
