package net.eulerframework.web.module.file.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.module.file.listener.FileStoreListener;

@Order(1)
public class FileStoreBootstrap extends LogSupport implements WebApplicationInitializer {
    
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        this.logger.info("Executing file store bootstrap.");
        container.addListener(new FileStoreListener());
    }
}
