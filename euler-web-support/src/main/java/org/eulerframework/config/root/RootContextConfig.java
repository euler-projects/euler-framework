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
package org.eulerframework.config.root;

import javax.annotation.Resource;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import org.eulerframework.web.core.annotation.ApiEndpoint;
import org.eulerframework.web.core.annotation.JspController;

@Configuration
@ImportResource({"classpath*:config/beans.xml"})
@ComponentScan(
        basePackages = {"org.eulerframework.**.web"},
        excludeFilters = {@ComponentScan.Filter(Controller.class),
                          @ComponentScan.Filter(JspController.class),
                          @ComponentScan.Filter(ApiEndpoint.class)}
)
@EnableTransactionManagement
public class RootContextConfig implements TransactionManagementConfigurer {
    
    @Resource(name="transactionManager")
    PlatformTransactionManager transactionManager;
    
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return this.transactionManager;
    }
}
