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
package net.eulerframework.web.core.listener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.common.util.CommonUtils;
import net.eulerframework.common.util.io.file.SimpleFileIOUtils;

/**
 * @author cFrost
 *
 */
public abstract class JspDeployListener extends LogSupport implements ServletContextListener {

    private final static String PAGE_PATH = "META-INF/jsp";
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
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
                
                if(!je.getName().startsWith(PAGE_PATH) || je.isDirectory()) {
                    continue;
                }
                
                try(InputStream inputStream = jspJar.getInputStream(je)) {
                    String pagePath = je.getName();
                    this.logger.info("Load page file: {}/{}", jarFilePath, pagePath);
                    
                    String webRootRealPath = sce.getServletContext().getRealPath("/");
                    String destPath = webRootRealPath + "WEB-INF/jsp" + pagePath.substring(PAGE_PATH.length());
                    if (!new File(destPath).exists()) {
                        byte[] result = new byte[inputStream.available()];

                        int count = 0;
                        int tempInt;
                        while ((tempInt = inputStream.read()) != -1) {
                            result[count++] = (byte) tempInt;
                        }

                        System.out.println(new String(result));
                    } else {
                        this.logger.info("Load page file: {}", pagePath.substring(PAGE_PATH.length()));
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
