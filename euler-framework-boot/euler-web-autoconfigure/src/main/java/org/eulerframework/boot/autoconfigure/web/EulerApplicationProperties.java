/*
 * Copyright 2013-2019 the original author or authors.
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
package org.eulerframework.boot.autoconfigure.web;

import org.eulerframework.common.util.CommonUtils;
import org.eulerframework.common.util.StringUtils;
import org.eulerframework.web.util.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.function.Supplier;

@ConfigurationProperties(prefix = "euler.application")
public class EulerApplicationProperties implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String SPRING_APPLICATION_NAME = "spring.application.name";

    @Autowired
    private Environment environment;

    private static final String DEFAULT_RUNTIME_PATH_PREFIX = "/var/run";
    private static final String DEFAULT_TEMP_PATH_PREFIX = "/var/tmp";

    private String runtimePath;
    private String tmpPath;

    public String getRuntimePath() {
        return runtimePath;
    }

    public void setRuntimePath(String runtimePath) {
        this.runtimePath = CommonUtils.convertDirToUnixFormat(runtimePath, false);
    }

    public String getTmpPath() {
        return tmpPath;
    }

    public void setTmpPath(String tmpPath) {
        this.tmpPath = CommonUtils.convertDirToUnixFormat(tmpPath, false);
    }

    @Override
    public void afterPropertiesSet() {
        this.runtimePath = this.handlePath(
                this.runtimePath,
                () -> DEFAULT_RUNTIME_PATH_PREFIX + "/" + this.environment.getProperty(SPRING_APPLICATION_NAME),
                "euler.application.runtime-path");
        this.tmpPath = this.handlePath(
                this.tmpPath,
                () -> DEFAULT_TEMP_PATH_PREFIX + "/" + this.environment.getProperty(SPRING_APPLICATION_NAME),
                "euler.application.tmp-path");
    }

    private String handlePath(String path, Supplier<String> defaultIfPathIsEmpty, String propertyName) {
        if (StringUtils.isEmpty(path)) {
            path = defaultIfPathIsEmpty.get();
            logger.info("'{}' is not configured, use {} as the default.", propertyName, path);
        }

        path = CommonUtils.convertDirToUnixFormat(path, false);

//        Assert.isTrue(path.startsWith("/") || path.startsWith("file://"),
//                () -> String.format("'%s' must begin with 'file://' or '/'", propertyName));

        if (path.startsWith("file://")) {
            path = path.substring("file://".length());
        }

        if (!path.startsWith("/")) {
            String classPath = ClassLoader.getSystemResource("").getPath();
            path = CommonUtils.convertDirToUnixFormat(classPath, false) + "/" + path;
        } else if (SystemUtils.isWindows() && path.startsWith("/")) {
            //当配置的路径为*inx格式的绝对路径，且当前环境是Windows时，默认放在C盘
            logger.warn("Application is running under Windows. '{}' does not specify a partition, use C: for default", propertyName);
            path = "C:" + path;
        }

        logger.info("'{}' is '{}'.", propertyName, path);
        return path;
    }
}
