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
package org.eulerframework.web.module.authentication.boot;

import org.eulerframework.common.base.log.LogSupport;
import org.eulerframework.common.util.property.FilePropertySource;
import org.eulerframework.common.util.property.PropertyReader;
import org.eulerframework.config.EulerWebSupportConfig;
import org.eulerframework.web.config.WebConfig;
import org.eulerframework.web.module.authentication.conf.SecurityConfig;
import org.eulerframework.web.module.authentication.conf.SecurityConfigExternal;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URISyntaxException;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class HighestPrecedenceFiltersBootstrap extends LogSupport implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        try {
            FilePropertySource eulerFrameworkFilePropertySource = new FilePropertySource("/config.properties");
            eulerFrameworkFilePropertySource.addPropertyFile("file:" + EulerWebSupportConfig.getConfigPath());
            PropertyReader eulerFrameworkPropertyReader = new PropertyReader(eulerFrameworkFilePropertySource);
            SecurityConfig.setPropertyReader(eulerFrameworkPropertyReader);
        } catch (IOException | URISyntaxException e) {
            throw new ServletException("SecurityConfig init error", e);
        }

        try {
            FilePropertySource eulerFrameworkFilePropertySource = new FilePropertySource("/config.properties");
            eulerFrameworkFilePropertySource.addPropertyFile("file:" + EulerWebSupportConfig.getConfigPath());
            PropertyReader eulerFrameworkPropertyReader = new PropertyReader(eulerFrameworkFilePropertySource);
            SecurityConfigExternal.setPropertyReader(eulerFrameworkPropertyReader);
        } catch (IOException | URISyntaxException e) {
            throw new ServletException("SecurityConfigExternal init error", e);
        }
    }
}
