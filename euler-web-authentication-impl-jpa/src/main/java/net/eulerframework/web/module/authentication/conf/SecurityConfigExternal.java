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
package net.eulerframework.web.module.authentication.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import net.eulerframework.cache.inMemoryCache.ObjectCachePool;
import net.eulerframework.common.util.property.PropertyReader;
import net.eulerframework.web.config.WebConfig;

public abstract class SecurityConfigExternal {
    protected static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    private static final DefaultObjectCache<String, Object> CONFIG_CAHCE = ObjectCachePool
            .generateDefaultObjectCache(Long.MAX_VALUE);

    private static final PropertyReader properties = new PropertyReader("/config.properties");

    private static class WebConfigKey {
        private static final String SECURITY_RESET_PASSWD_PRIV_KEY = "security.resetPassword.privKeyFile";
        private static final String SECURITY_RESET_PASSWD_PUB_KEY = "security.resetPassword.pubKeyFile";
    }

    private static class WebConfigDefault {
        private static final String SECURITY_RESET_PASSWD_PRIV_KEY = "rsa/resetPasswdPrivKey.pem";
        private static final String SECURITY_RESET_PASSWD_PUB_KEY = "rsa/resetPasswdPubKey.pem";
    }

    public static boolean clearSecurityConfigCache() {
        properties.refresh();
        return CONFIG_CAHCE.clear();
    }
    
    public static String getResetPasswordPrivKeyFile() {
        return WebConfig.getRuntimePath() + "/" + (String) CONFIG_CAHCE.get(WebConfigKey.SECURITY_RESET_PASSWD_PRIV_KEY, 
                key -> properties.get(key, WebConfigDefault.SECURITY_RESET_PASSWD_PRIV_KEY));
    }
    
    public static String getResetPasswordPubKeyFile() {
        return WebConfig.getRuntimePath() + "/" + (String) CONFIG_CAHCE.get(WebConfigKey.SECURITY_RESET_PASSWD_PUB_KEY, 
                key -> properties.get(key, WebConfigDefault.SECURITY_RESET_PASSWD_PUB_KEY));
    }

}
