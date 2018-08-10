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
package net.eulerframework.web.core.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import net.eulerframework.cache.inMemoryCache.ObjectCachePool;
import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.config.WebConfig;

public class EulerFrameworkCoreListener extends LogSupport implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.logger.info("Init euler framework core listener");
        
        ObjectCachePool.initEulerCachePoolCleaner(60_000, WebConfig.getRamCacheCleanFreq());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}