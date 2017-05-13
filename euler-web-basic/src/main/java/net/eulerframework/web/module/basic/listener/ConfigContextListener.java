package net.eulerframework.web.module.basic.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import net.eulerframework.web.module.basic.context.DBConfigContext;
import net.eulerframework.web.module.basic.service.ConfigService;

@Component
public class ConfigContextListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        
        WebApplicationContext rwp = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());        
        ConfigService configService= (ConfigService)rwp.getBean("configService");        
        DBConfigContext.setConfigService(configService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
