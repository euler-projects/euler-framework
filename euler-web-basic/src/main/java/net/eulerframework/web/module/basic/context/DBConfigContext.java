package net.eulerframework.web.module.basic.context;

import net.eulerframework.cache.inMemoryCache.DataNotFoundException;
import net.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import net.eulerframework.cache.inMemoryCache.ObjectCachePool;
import net.eulerframework.web.module.basic.entity.Config;
import net.eulerframework.web.module.basic.exception.ConfigNotFoundException;
import net.eulerframework.web.module.basic.service.ConfigService;

public class DBConfigContext {
    
    private final static DefaultObjectCache<String, String> CONF_CACHE = ObjectCachePool.generateDefaultObjectCache(60_000);
    
    private static ConfigService configService;

    public static void setConfigService(ConfigService configService) {
        DBConfigContext.configService = configService;
    }

    public static String getConfig(String key) throws ConfigNotFoundException {
        String result;
        try {
            result = CONF_CACHE.get(key);
        } catch (DataNotFoundException e) {
            Config config = configService.findConfig(key);
            
            if(config == null)
                throw new ConfigNotFoundException("config '" + key + "' not found");
            
            result = config.getValue();
            
            CONF_CACHE.put(key, result);
        }
        
        return result;
    }
}
