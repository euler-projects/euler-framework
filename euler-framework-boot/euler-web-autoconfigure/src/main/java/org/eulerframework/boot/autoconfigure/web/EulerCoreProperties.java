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
import org.eulerframework.web.util.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "euler.core")
public class EulerCoreProperties implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String DEFAULT_RUNTIME_PATH_UNIX = "file:///var/lib/euler-framework";

    private String runtimePath = DEFAULT_RUNTIME_PATH_UNIX;
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
        if (!this.runtimePath.startsWith("/") && !this.runtimePath.startsWith("file://")) {
            throw new RuntimeException("'euler.core.runtime-path' must begin with 'file://' or '/'");
        } else {
            if (this.runtimePath.startsWith("file://")) {
                this.runtimePath = this.runtimePath.substring("file://".length());
            }
        }

        //当配置的路径为*inx格式，但是当前环境是Windows时，默认放在C盘
        if(SystemUtils.isWindows() && this.runtimePath.startsWith("/")) {
            logger.warn("Application is running  under Windows. 'euler.core.runtime-path' does not specify a partition, use C: for default");
            this.runtimePath = "C:" + this.runtimePath;
        }

        if (this.tmpPath == null) {
            this.tmpPath = this.runtimePath + "/tmp";
            logger.info("'euler.core.tmp-path' is not configured, use {} for default", this.tmpPath);
        }
    }
}
