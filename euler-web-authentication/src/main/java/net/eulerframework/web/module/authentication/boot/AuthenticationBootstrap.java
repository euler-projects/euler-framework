package net.eulerframework.web.module.authentication.boot;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.module.authentication.listener.UserContextListener;

@Order(300)
public class AuthenticationBootstrap extends LogSupport implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        this.logger.info("Executing Authentication Bootstrap.");
        container.addListener(new UserContextListener());
    }
}
