/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015-2016 cFrost.sun(孙宾, SUN BIN) 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following website
 * 
 * https://github.com/euler-form/web-form
 * http://eulerform.net
 * http://cfrost.net
 */
package net.eulerframework.web.core.listener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.eulerframework.web.core.util.WebConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import net.eulerframework.common.util.FilePathTool;
import net.eulerframework.common.util.FileReader;

@Component
public class EulerFormCoreListener implements ServletContextListener {
    private static final Logger log = LogManager.getLogger();

    private final static String JAR_JSP_PATH = "classpath*:**/web/module/*/META-INF/pages/*";
    private final static String JAR_MODULE_JSP_FOLDER = "/META-INF/pages";
    private final static String MODULE_JSP_FOLDER;

    static {
        MODULE_JSP_FOLDER = WebConfig.getJspPath();
    }

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    
    private boolean enableJspAutoDeploy = false;
    
    public EulerFormCoreListener() {
        this.enableJspAutoDeploy = WebConfig.isJspAutoDeployEnabled();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if(this.enableJspAutoDeploy)
            this.initJarJspPages(sce);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // TODO Auto-generated method stub

    }

    private void initJarJspPages(ServletContextEvent sce) {
        Resource[] resources = null;
        InputStream inputStream = null;
        try {
            resources = this.resourcePatternResolver.getResources(JAR_JSP_PATH);

            for (Resource r : resources) {
                inputStream = r.getInputStream();
                String jspPath = r.getURI().toString();
                // like "/index.jsp"
                String filename = jspPath
                        .substring(jspPath.lastIndexOf(JAR_MODULE_JSP_FOLDER) + JAR_MODULE_JSP_FOLDER.length());
                String tmp = jspPath.substring(0, jspPath.lastIndexOf(JAR_MODULE_JSP_FOLDER));
                // like "/demoModule"
                String moduleName = tmp.substring(tmp.lastIndexOf("/"));
                // like "/var/www/demo" or "D:/website"
                String webRootRealPath = FilePathTool.changeToUnixFormat(sce.getServletContext().getRealPath("/"));
                String destPath = webRootRealPath + MODULE_JSP_FOLDER + moduleName + filename;
                if (!new File(destPath).exists()) {
                    byte[] result = FileReader.readInputStreamByMultiBytes(inputStream, 1024);
                    FileReader.writeFile(destPath, result, false);
                    log.info("\nImport module jsp file: \n" + r.getURI().toString() + "\ninto\n" + destPath + "\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }
    }

}
