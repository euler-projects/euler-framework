package net.eulerframework.web.module.basic.listener;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import net.eulerframework.web.module.basic.service.IDictionaryService;
import net.eulerframework.web.module.basic.service.impl.DictionaryService;

@Component
public class BasicListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        
        WebApplicationContext rwp = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
        
        IDictionaryService dictionaryService= (DictionaryService)rwp.getBean("dictionaryService");
        try {
            dictionaryService.createCodeDict();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        dictionaryService.loadBaseData();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
