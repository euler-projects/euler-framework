package net.eulerform.web.module.basedata.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import net.eulerform.web.module.basedata.listener.CodeTableListener;

@Order(1)
public class CodeTableBootstrap implements WebApplicationInitializer {
    private final Logger log = LogManager.getLogger();
    
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        log.info("Executing CodeTable bootstrap.");
        container.addListener(new CodeTableListener());
    }
}
