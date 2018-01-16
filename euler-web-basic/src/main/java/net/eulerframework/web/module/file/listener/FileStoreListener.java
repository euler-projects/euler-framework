package net.eulerframework.web.module.file.listener;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.stereotype.Component;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.module.file.conf.FileConfig;

@Component
public class FileStoreListener extends LogSupport implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String archiveFilePath = FileConfig.getFileArchivedPath();   

        File targetDir = new File(archiveFilePath);
        
        if(!targetDir.exists()) {
            targetDir.mkdirs();
            this.logger.info("Create File Archive Dir: " + targetDir.getAbsolutePath());
        } else {
            this.logger.info("File Archive Dir exist: " + targetDir.getAbsolutePath());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
