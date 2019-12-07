package org.eulerframework.web.config;

import junit.framework.Assert;
import org.eulerframework.common.util.CommonUtils;
import org.eulerframework.web.util.SystemUtils;
import org.junit.Test;

import java.nio.file.FileSystemException;

public class ConfigUtilsTest {

    @Test
    public void handleApplicationPath() throws FileSystemException {
        String classPath = CommonUtils.convertDirToUnixFormat(ClassLoader.getSystemResource("").getPath(), false);

        if(SystemUtils.isWindows()) {
            Assert.assertEquals("C:/var/run", ConfigUtils.handleApplicationPath("file:///var/run", null, "unit.test", false));
            Assert.assertEquals("C:/var/run", ConfigUtils.handleApplicationPath("/var/run", null, "unit.test", false));
            Assert.assertEquals("C:/var/run", ConfigUtils.handleApplicationPath("C:/var/run", null, "unit.test", false));
            Assert.assertEquals("C:/var/run", ConfigUtils.handleApplicationPath("C:\\var\\run", null, "unit.test", false));
        } else {
            Assert.assertEquals("/var/run", ConfigUtils.handleApplicationPath("file:///var/run", null, "unit.test", false));
            Assert.assertEquals("/var/run", ConfigUtils.handleApplicationPath("/var/run", null, "unit.test", false));
            Assert.assertEquals(classPath + "/C:/var/run", ConfigUtils.handleApplicationPath("C:/var/run", null, "unit.test", false));
            Assert.assertEquals(classPath + "/C:/var/run", ConfigUtils.handleApplicationPath("C:\\var\\run", null, "unit.test", false));
        }

        Assert.assertEquals(classPath + "/var/run", ConfigUtils.handleApplicationPath("file://var/run", null, "unit.test", false));
        Assert.assertEquals(classPath + "/var/run", ConfigUtils.handleApplicationPath("var/run", null, "unit.test", false));
    }

}