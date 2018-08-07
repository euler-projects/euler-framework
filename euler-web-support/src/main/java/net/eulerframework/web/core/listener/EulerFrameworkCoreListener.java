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
