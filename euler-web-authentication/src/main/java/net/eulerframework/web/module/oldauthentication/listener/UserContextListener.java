package net.eulerframework.web.module.oldauthentication.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import net.eulerframework.web.module.oldauthentication.context.UserContext;
import net.eulerframework.web.module.oldauthentication.service.UserService;

@Component
public class UserContextListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        
        WebApplicationContext rwp = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());        
        UserService userService= (UserService)rwp.getBean("userService");        
        UserContext.setUserService(userService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
