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
 * https://eulerproject.io
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
