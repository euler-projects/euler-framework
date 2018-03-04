package net.eulerframework.web.module.authentication.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.module.authentication.context.UserContext;
import net.eulerframework.web.module.authentication.service.EulerUserDetailsService;
import net.eulerframework.web.module.authentication.service.EulerUserEntityService;
import net.eulerframework.web.module.authentication.util.SecurityTag;
import net.eulerframework.web.module.authentication.util.UserDataValidator;

public class UserContextListener extends LogSupport implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        WebApplicationContext rwp = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
        EulerUserDetailsService userDetailsService= rwp.getBean("userDetailsService", EulerUserDetailsService.class);
        UserContext.setUserDetailsServicel(userDetailsService);
        EulerUserEntityService eulerUserEntityService= rwp.getBean(EulerUserEntityService.class);
        UserContext.setEulerUserEntityService(eulerUserEntityService);
        UserDataValidator.setEulerUserEntityService(eulerUserEntityService);
        SecurityTag.setEulerUserEntityService(eulerUserEntityService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
