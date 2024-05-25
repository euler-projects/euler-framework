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
package org.eulerframework.web.module.oauth2.conf;

import org.eulerframework.common.util.property.FilePropertySource;
import org.eulerframework.web.config.WebConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import org.eulerframework.cache.inMemoryCache.ObjectCachePool;
import org.eulerframework.common.util.property.PropertyReader;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class OAuth2ServerConfig {
    protected static final Logger LOGGER = LoggerFactory.getLogger(OAuth2ServerConfig.class);

    private static final DefaultObjectCache<String, Object> CONFIG_CAHCE = ObjectCachePool
            .generateDefaultObjectCache(Long.MAX_VALUE);

    private static PropertyReader propertyReader;

    static {
        try {
            propertyReader = new PropertyReader(new FilePropertySource(WebConfig.DEFAULT_CONFIG_FILE));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setPropertyReader(PropertyReader propertyReader) {
        OAuth2ServerConfig.propertyReader = propertyReader;
    }

    public static PropertyReader getPropertyReader() {
        return propertyReader;
    }

    private static class OAuth2ServerConfigKey {
        public static final String SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_LIFE = "security.auth2.clientDetails.cacheLife";
        public static final String SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_ENABLED = "security.auth2.clientDetails.cache.enabled";
    }

    private static class OAuth2ServerConfigDefault {
        public static final long SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_LIFE = 10_000L;
        public static final boolean SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_ENABLED = false;
    }

    /**
     * @return
     */
    public static long getClientDetailsCacheLife() {
        return (long) CONFIG_CAHCE.get(OAuth2ServerConfigKey.SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_LIFE,
                configKey -> propertyReader.getLongValue(configKey,
                        OAuth2ServerConfigDefault.SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_LIFE));
    }

    /**
     * @return
     */
    public static boolean isEnableClientDetailsCache() {
        return (boolean) CONFIG_CAHCE.get(OAuth2ServerConfigKey.SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_ENABLED,
                configKey -> propertyReader.getBooleanValue(configKey,
                        OAuth2ServerConfigDefault.SECURITY_OAUTH2_CLIENT_DETAILS_CAHCE_ENABLED));
    }

}
