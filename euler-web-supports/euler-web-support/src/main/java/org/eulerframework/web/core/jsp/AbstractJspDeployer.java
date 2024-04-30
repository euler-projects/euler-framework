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
package org.eulerframework.web.core.jsp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.eulerframework.common.base.log.LogSupport;
import org.eulerframework.common.util.CommonUtils;
import org.eulerframework.common.util.io.file.SimpleFileIOUtils;

/**
 * @author cFrost
 *
 */
public abstract class AbstractJspDeployer extends LogSupport implements ServletContextListener {

    private final static String PACKAGE_PAGE_PATH = "META-INF/jsp";
    private final static String RUNTIME_PAGE_PATH = "WEB-INF/jsp";
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.deployJsps(sce);
    }
    
    protected void deployJsps(ServletContextEvent sce) {
        this.logger.info("Jar deployer was found: {}", this.getClass().getName());
        
        /**
         * 得到当前类(JspDeployListener的实现类)所在的完整绝对路径，不以/结尾
         */
        String classAbsolutePath = CommonUtils.convertDirToUnixFormat(this.getClass().getResource("").toString(), false);
        
        /**
         * 得到当前类在Jar包内的路径，以/头，不以/结尾
         */
        Package pachage = this.getClass().getPackage();
        String classRelativePath = "/" + pachage.getName().replaceAll("\\.", "/");
        
        String jarFilePath = classAbsolutePath.substring(0,  classAbsolutePath.length() - classRelativePath.length() - 1).substring("jar:file:".length());
        
        this.logger.info("Jar deployer path: {}", jarFilePath);
        
        try(JarFile jspJar = new JarFile(jarFilePath)) {
            Enumeration<JarEntry> jes = jspJar.entries();
            while(jes.hasMoreElements()) {
                JarEntry je = jes.nextElement();
                
                if(!je.getName().startsWith(PACKAGE_PAGE_PATH) || je.isDirectory()) {
                    continue;
                }
                
                try(InputStream inputStream = jspJar.getInputStream(je)) {
                    String pagePath = je.getName();
                    this.logger.info("Find package page file: {}/{}", jarFilePath, pagePath);
                    
                    String webRootRealPath = sce.getServletContext().getRealPath("/");
                    String destPath = webRootRealPath + RUNTIME_PAGE_PATH + pagePath.substring(PACKAGE_PAGE_PATH.length());
                    if (!new File(destPath).exists()) {
                        byte[] result = new byte[inputStream.available()];

                        int count = 0;
                        int tempInt;
                        while ((tempInt = inputStream.read()) != -1) {
                            result[count++] = (byte) tempInt;
                        }
                        
                        SimpleFileIOUtils.writeFile(destPath, result, false);
                        this.logger.info("Page file deployed: {}/{} -> {}", jarFilePath, pagePath, destPath);
                    } else {
                        this.logger.info("Page file existed, deploy ignored: {}/{}", jarFilePath, pagePath);
                    }
                } 
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // TODO Auto-generated method stub

    }

}
