/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
