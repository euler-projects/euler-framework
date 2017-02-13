package net.eulerframework.web.module.authentication.bootstrap;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.module.authentication.filter.AuthenticateInfoFilter;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@Order(300)
public class AuthenticateBootstrap extends LogSupport implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        this.logger.info("Executing authenticate logging bootstrap.");

        FilterRegistration.Dynamic authenticateInfoFilter = container.addFilter("authenticateInfoFilter", new AuthenticateInfoFilter("oauth/token","oauth/check_token"));
        authenticateInfoFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}
