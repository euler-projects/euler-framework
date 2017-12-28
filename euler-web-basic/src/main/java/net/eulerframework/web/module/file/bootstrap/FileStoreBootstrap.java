package net.eulerframework.web.module.file.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.constant.EulerSysAttributes;
import net.eulerframework.web.module.file.listener.FileStoreListener;

@Order(1)
public class FileStoreBootstrap extends LogSupport implements WebApplicationInitializer {
    
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        this.logger.info("Executing file store bootstrap.");
        container.addListener(new FileStoreListener());
        
        String contextPath = container.getContextPath();


        container.setAttribute(EulerSysAttributes.FILE_DOWNLOAD_PATH_ATTR.value(), contextPath + "/file");
        container.setAttribute(EulerSysAttributes.FILE_UPLOAD_ACTION_ATTR.value(), contextPath + "/uploadFile");

    }
}
