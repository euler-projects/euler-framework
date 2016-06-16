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
