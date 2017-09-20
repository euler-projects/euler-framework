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
        
        String contextPath = container.getContextPath();

        container.setAttribute("__FILE_DOWNLOAD_PATH", contextPath + "/file");
        container.setAttribute("__IMAGE_DOWNLOAD_PATH", contextPath + "/image");
        container.setAttribute("__FILE_UPLOAD_ACTION", contextPath + "/uploadFile");
        
        container.addListener(new FileStoreListener());
    }
}
