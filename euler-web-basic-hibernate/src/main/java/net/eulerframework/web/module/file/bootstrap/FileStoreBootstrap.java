package net.eulerframework.web.module.file.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.module.file.conf.FileConfig;
import net.eulerframework.web.module.file.listener.FileStoreListener;

@Order(1)
public class FileStoreBootstrap extends LogSupport implements WebApplicationInitializer {
    
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        this.logger.info("Executing file store bootstrap.");
        
        String contextPath = container.getContextPath();

        container.setAttribute(FileConfig.FILE_DOWNLOAD_PATH_ATTR, contextPath + "/file");
        container.setAttribute(FileConfig.IMAGE_DOWNLOAD_PATH_ATTR, contextPath + "/image");
        container.setAttribute(FileConfig.FILE_UPLOAD_ACTION_ATTR, contextPath + "/uploadFile");
        
        container.addListener(new FileStoreListener());
    }
}
