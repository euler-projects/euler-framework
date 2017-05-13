package net.eulerframework.web.module.basic.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.module.basic.listener.ConfigContextListener;

@Order(1)
public class BasicBootstrap extends LogSupport implements WebApplicationInitializer {
    
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        this.logger.info("Executing Basic bootstrap.");
        container.addListener(new ConfigContextListener());
    }
}
