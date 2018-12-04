/*
 * Copyright 2013-2018 the original author or authors.
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
package org.eulerframework.web.module.authentication.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.eulerframework.common.base.log.LogSupport;
import org.eulerframework.web.module.authentication.context.UserContext;
import org.eulerframework.web.module.authentication.service.EulerUserDetailsService;
import org.eulerframework.web.module.authentication.service.EulerUserEntityService;
import org.eulerframework.web.module.authentication.util.SecurityTag;
import org.eulerframework.web.module.authentication.util.UserDataValidator;

public class UserContextListener extends LogSupport implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        WebApplicationContext rwp = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
        EulerUserDetailsService userDetailsService= rwp.getBean("userDetailsService", EulerUserDetailsService.class);
        UserContext.setUserDetailsServicel(userDetailsService);
        EulerUserEntityService eulerUserEntityService= rwp.getBean(EulerUserEntityService.class);
        UserContext.setEulerUserEntityService(eulerUserEntityService);
        UserDataValidator.setEulerUserEntityService(eulerUserEntityService);
        SecurityTag.setEulerUserEntityService(eulerUserEntityService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
