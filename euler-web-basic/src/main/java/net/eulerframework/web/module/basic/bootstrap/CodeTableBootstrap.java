package net.eulerframework.web.module.basic.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import net.eulerframework.web.module.basic.listener.BaseDataListener;

@Order(1)
public class CodeTableBootstrap implements WebApplicationInitializer {
    private final Logger log = LogManager.getLogger();
    
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        log.info("Executing CodeTableTool bootstrap.");
        container.addListener(new BaseDataListener());
    }
}
