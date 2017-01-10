package net.eulerframework.web.module.file.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import net.eulerframework.web.module.file.listener.FileStoreListener;

@Order(1)
public class FileStoreBootstrap implements WebApplicationInitializer {
    private final Logger logger = LogManager.getLogger();
    
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        logger.info("Executing file store bootstrap.");
        container.addListener(new FileStoreListener());
    }
}
