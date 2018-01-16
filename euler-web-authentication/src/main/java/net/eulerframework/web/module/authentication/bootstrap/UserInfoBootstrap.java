package net.eulerframework.web.module.authentication.bootstrap;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.constant.EulerSysAttributes;
import net.eulerframework.web.module.authentication.filter.UserInfoFilter;
import net.eulerframework.web.module.authentication.listener.UserContextListener;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@Order(300)
public class UserInfoBootstrap extends LogSupport implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        this.logger.info("Executing user info bootstrap.");
        
        String contextPath = container.getContextPath();
        container.setAttribute(EulerSysAttributes.SIGN_OUT_ACTION.value(), contextPath + "/signout");
        
        
        container.addListener(new UserContextListener());

        FilterRegistration.Dynamic authenticateInfoFilter = container.addFilter("authenticateInfoFilter", new UserInfoFilter("oauth/token","oauth/check_token"));
        authenticateInfoFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}
