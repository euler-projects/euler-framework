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

import org.eulerframework.common.util.property.FilePropertySource;
import org.eulerframework.common.util.property.PropertyNotFoundException;
import org.eulerframework.common.util.property.PropertyReader;

import java.io.IOException;
import java.net.URISyntaxException;

public class SystemUtils {

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static String frameworkVersion() {
        return SystemProperties.frameworkVersion();
    }

    /**
     * 用户获取系统参数
     *
     * @author cFrost
     */
    private static final class SystemProperties {

        private final static PropertyReader properties;

        static {
            try {
                properties = new PropertyReader(new FilePropertySource("/system.properties"));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        public static String frameworkVersion() {
            try {
                return properties.getString("version");
            } catch (PropertyNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
