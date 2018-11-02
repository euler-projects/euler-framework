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
package org.eulerframework.web.module.file.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import org.eulerframework.common.base.log.LogSupport;
import org.eulerframework.constant.EulerSysAttributes;
import org.eulerframework.web.module.file.conf.FileConfig;
import org.eulerframework.web.module.file.listener.FileStoreListener;

@Order(1)
public class FileStoreBootstrap extends LogSupport implements WebApplicationInitializer {
    
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        this.logger.info("Executing file store bootstrap.");
        
        String contextPath = container.getContextPath();

        container.setAttribute(EulerSysAttributes.FILE_DOWNLOAD_PATH_ATTR.value(), contextPath + FileConfig.FILE_DOWNLOAD_PATH);
        container.setAttribute(EulerSysAttributes.IMAGE_DOWNLOAD_PATH_ATTR.value(), contextPath + FileConfig.IMAGE_DOWNLOAD_PATH);
        container.setAttribute(EulerSysAttributes.VIDEO_DOWNLOAD_PATH_ATTR.value(), contextPath + FileConfig.VIDEO_DOWNLOAD_PATH);
        container.setAttribute(EulerSysAttributes.FILE_UPLOAD_ACTION_ATTR.value(), contextPath + FileConfig.FILE_UPLOAD_ACTION);
        
        container.addListener(new FileStoreListener());
    }
}
