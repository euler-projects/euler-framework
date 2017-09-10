package net.eulerframework.web.module.authentication.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import net.eulerframework.web.module.authentication.context.UserContext;
import net.eulerframework.web.module.authentication.service.EulerUserDetailsService;

public class UserContextListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        WebApplicationContext rwp = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());        
        EulerUserDetailsService userDetailsService= (EulerUserDetailsService)rwp.getBean("userDetailsService");        
        UserContext.setUserDetailsServicel(userDetailsService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
