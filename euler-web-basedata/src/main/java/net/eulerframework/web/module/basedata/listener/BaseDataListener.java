package net.eulerframework.web.module.basedata.listener;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.eulerframework.web.module.basedata.service.IBaseDataService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import net.eulerframework.web.module.basedata.service.impl.BaseDataService;

@Component
public class BaseDataListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        
        WebApplicationContext rwp = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
        
        IBaseDataService baseDataService= (BaseDataService)rwp.getBean("baseDataService");
        
        String webRootRealPath = sce.getServletContext().getRealPath("/");
        baseDataService.setWebRootRealPath(webRootRealPath);
        try {
            baseDataService.createCodeDict();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        baseDataService.loadBaseData();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // TODO Auto-generated method stub

    }

}
