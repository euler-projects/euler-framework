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
package org.eulerframework.web.module.authentication.boot;

import jakarta.servlet.ServletContext;

import org.eulerframework.config.EulerWebSupportConfig;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

import org.eulerframework.boot.EulerFrameworkBootstrap;
import org.eulerframework.web.config.WebConfig;
import org.eulerframework.web.module.authentication.conf.SecurityConfiguration;

@Order(-1)
public class SecurityBootstrap extends AbstractSecurityWebApplicationInitializer {

    public SecurityBootstrap() throws ClassNotFoundException {
        super(Class.forName(EulerWebSupportConfig.getRootContextConfigClassName()), SecurityConfiguration.class);
    }

    @Override
    protected boolean enableHttpSessionEventPublisher() {
        return true;
    }

    @Override
    protected void afterSpringSecurityFilterChain(ServletContext servletContext) {
        servletContext.setAttribute(EulerFrameworkBootstrap.EULER_SPRING_SECURITY_ENABLED, true);
    }
}
