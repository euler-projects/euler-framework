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
import jakarta.servlet.ServletException;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import org.eulerframework.common.base.log.LogSupport;
import org.eulerframework.constant.EulerSysAttributes;
import org.eulerframework.web.module.authentication.listener.UserContextListener;

@Order(300)
public class AuthenticationBootstrap extends LogSupport implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        this.logger.info("Executing Authentication Bootstrap.");
        container.addListener(new UserContextListener());
        
        //TODO:完善注销action获取逻辑
        String contextPath = container.getContextPath();
        container.setAttribute(EulerSysAttributes.SIGN_OUT_ACTION.value(), contextPath + "/signout");
    }
}
