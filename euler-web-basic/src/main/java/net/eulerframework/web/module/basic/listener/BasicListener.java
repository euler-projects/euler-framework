package net.eulerframework.web.module.basic.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.stereotype.Component;

@Component
public class BasicListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        
//        WebApplicationContext rwp = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
//        
//        DictionaryService dictionaryService= (DictionaryService)rwp.getBean("dictionaryService");
//        
//        dictionaryService.loadBaseData();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
