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
package org.eulerframework.web.module.basic.context;

import org.eulerframework.cache.inMemoryCache.DataNotFoundException;
import org.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import org.eulerframework.cache.inMemoryCache.ObjectCachePool;
import org.eulerframework.web.module.basic.entity.Config;
import org.eulerframework.web.module.basic.exception.ConfigNotFoundException;
import org.eulerframework.web.module.basic.htservice.ConfigService;

public class DBConfigContext {
    
    private final static DefaultObjectCache<String, String> CONF_CACHE = ObjectCachePool.generateDefaultObjectCache(60_000);
    
    private static ConfigService configService;

    public static void setConfigService(ConfigService configService) {
        DBConfigContext.configService = configService;
    }
    
    public static void clear() {
        while(!CONF_CACHE.clear());
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
