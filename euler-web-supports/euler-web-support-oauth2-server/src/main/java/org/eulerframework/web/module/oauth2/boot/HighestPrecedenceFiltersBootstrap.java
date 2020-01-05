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
package org.eulerframework.web.module.oauth2.boot;

import org.eulerframework.common.base.log.LogSupport;
import org.eulerframework.common.util.property.FilePropertySource;
import org.eulerframework.common.util.property.PropertyReader;
import org.eulerframework.common.util.property.PropertySource;
import org.eulerframework.web.config.WebConfig;
import org.eulerframework.web.module.oauth2.conf.OAuth2ServerConfig;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URISyntaxException;

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class HighestPrecedenceFiltersBootstrap extends LogSupport implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        try {
            PropertySource propertySource = OAuth2ServerConfig.getPropertyReader().getPropertySource();
            if (FilePropertySource.class.isAssignableFrom(propertySource.getClass())) {
                FilePropertySource eulerFrameworkFilePropertySource = (FilePropertySource) propertySource;
                eulerFrameworkFilePropertySource.addPropertyFile("file:" + WebConfig.getAdditionalConfigPath() + WebConfig.DEFAULT_CONFIG_FILE);
            }
        } catch (IOException | URISyntaxException e) {
            throw new ServletException("OAuth2ServerConfig init error", e);
        }
    }
}