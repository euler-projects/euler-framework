/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2018 Euler Project 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following website
 * 
 * http://cfrost.net
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
