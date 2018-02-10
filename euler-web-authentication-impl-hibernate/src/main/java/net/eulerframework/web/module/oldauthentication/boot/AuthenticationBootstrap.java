package net.eulerframework.web.module.oldauthentication.boot;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.core.filter.RequestIdFilter;
import net.eulerframework.web.module.oldauthentication.filter.UserInfoFilter;
import net.eulerframework.web.module.oldauthentication.listener.UserContextListener;

@Order(300)
public class AuthenticationBootstrap extends LogSupport implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        this.logger.info("Executing Authentication Bootstrap.");
        container.addListener(new UserContextListener());
        
        FilterRegistration.Dynamic userInfoFilter = container.addFilter("userInfoFilter", new UserInfoFilter());
        userInfoFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}
