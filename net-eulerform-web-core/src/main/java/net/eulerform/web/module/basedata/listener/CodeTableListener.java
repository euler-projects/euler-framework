package net.eulerform.web.module.basedata.listener;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import net.eulerform.web.module.authentication.util.UserContext;
import net.eulerform.web.module.basedata.service.impl.CodeTableService;

@Component
public class CodeTableListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        
        UserContext.addSpecialSystemSecurityContext();
        
        WebApplicationContext rwp = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
        CodeTableService bean= (CodeTableService)rwp.getBean("codeTableService");
        String webRootRealPath = sce.getServletContext().getRealPath("/");
        bean.setWebRootRealPath(webRootRealPath);
        try {
            bean.createCodeDict();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // TODO Auto-generated method stub

    }

}
