/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.util;

import junit.framework.Assert;
import org.eulerframework.common.util.CommonUtils;
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
//            Assert.assertEquals(classPath + "/C:/var/run", ConfigUtils.handleApplicationPath("C:/var/run", null, "unit.test", false));
//            Assert.assertEquals(classPath + "/C:/var/run", ConfigUtils.handleApplicationPath("C:\\var\\run", null, "unit.test", false));
        }

//        Assert.assertEquals(classPath + "/var/run", ConfigUtils.handleApplicationPath("file://var/run", null, "unit.test", false));
//        Assert.assertEquals(classPath + "/var/run", ConfigUtils.handleApplicationPath("var/run", null, "unit.test", false));
    }

}