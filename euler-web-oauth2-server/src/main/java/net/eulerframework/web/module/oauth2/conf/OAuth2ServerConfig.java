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
package net.eulerframework.web.module.oauth2.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import net.eulerframework.cache.inMemoryCache.ObjectCachePool;
import net.eulerframework.common.util.property.PropertyReader;

public abstract class OAuth2ServerConfig {
    protected static final Logger LOGGER = LoggerFactory.getLogger(OAuth2ServerConfig.class);

    private static final DefaultObjectCache<String, Object> CONFIG_CAHCE = ObjectCachePool
            .generateDefaultObjectCache(Long.MAX_VALUE);

    private static final PropertyReader properties = new PropertyReader("/config.properties");

    private static class OAuth2ServerConfigKey {
        public static final String SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_LIFE = "security.auth2.clientDetails.cacheLife";
        public static final String SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_ENABLED = "security.auth2.clientDetails.cache.enabled";
    }

    private static class OAuth2ServerConfigDefault {
        public static final long SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_LIFE = 10_000L;
        public static final boolean SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_ENABLED = false;
    }

    public static boolean clearOAuth2ServerConfigCache() {
        properties.refresh();
        return CONFIG_CAHCE.clear();
    }

    /**
     * @return
     */
    public static long getClientDetailsCacheLife() {
        return (long) CONFIG_CAHCE.get(OAuth2ServerConfigKey.SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_LIFE,
                configKey -> properties.getLongValue(configKey,
                        OAuth2ServerConfigDefault.SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_LIFE));
    }

    /**
     * @return
     */
    public static boolean isEnableClientDetailsCache() {
        return (boolean) CONFIG_CAHCE.get(OAuth2ServerConfigKey.SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_ENABLED,
                configKey -> properties.getBooleanValue(configKey,
                        OAuth2ServerConfigDefault.SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_ENABLED));
    }

}
