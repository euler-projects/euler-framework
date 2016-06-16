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
package net.eulerform.web.core.listener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import net.eulerform.common.FileReader;

@Component
public class EulerFormCoreListener implements ServletContextListener {
    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Resource[] resources = null;
        InputStream inputStream = null;
        try {
            resources = this.resourcePatternResolver.getResources("classpath*:**/web/module/*/META-INF/pages/*");

            for (Resource r : resources) {
                inputStream = r.getInputStream();
                String jspPath = r.getURI().toString();
                String filename = jspPath
                        .substring(jspPath.lastIndexOf("/META-INF/pages/") + "/META-INF/pages/".length());
                String tmp = jspPath.substring(0, jspPath.lastIndexOf("/META-INF/pages/"));
                String moduleName = tmp.substring(tmp.lastIndexOf("/"));
                String webRootRealPath = sce.getServletContext().getRealPath("/");
                String destPaht = webRootRealPath + "WEB-INF/modulePages" + moduleName + "/" + filename;
                if (!new File(destPaht).exists()) {
                    byte[] result = FileReader.readInputStreamByMultiBytes(inputStream, 1024);
                    FileReader.writeFile(destPaht, result);
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

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // TODO Auto-generated method stub

    }

}
