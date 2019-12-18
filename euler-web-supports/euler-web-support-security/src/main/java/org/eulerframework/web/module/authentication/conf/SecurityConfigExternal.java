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
package org.eulerframework.web.module.authentication.conf;

import org.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import org.eulerframework.cache.inMemoryCache.ObjectCachePool;
import org.eulerframework.common.util.property.FilePropertySource;
import org.eulerframework.common.util.property.PropertyReader;
import org.eulerframework.web.config.WebConfig;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class SecurityConfigExternal {

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
        SecurityConfigExternal.propertyReader = propertyReader;
    }

    public static PropertyReader getPropertyReader() {
        return propertyReader;
    }

    private static class WebConfigKey {
        private static final String SECURITY_RESET_PASSWD_PRIV_KEY = "security.resetPassword.privKeyFile";
        private static final String SECURITY_RESET_PASSWD_PUB_KEY = "security.resetPassword.pubKeyFile";
    }

    private static class WebConfigDefault {
        private static final String SECURITY_RESET_PASSWD_PRIV_KEY = "rsa/resetPasswdPrivKey.pem";
        private static final String SECURITY_RESET_PASSWD_PUB_KEY = "rsa/resetPasswdPubKey.pem";
    }

    public static String getResetPasswordPrivKeyFile() {
        return WebConfig.getRuntimePath() + "/" + (String) CONFIG_CAHCE.get(WebConfigKey.SECURITY_RESET_PASSWD_PRIV_KEY,
                key -> propertyReader.getString(key, WebConfigDefault.SECURITY_RESET_PASSWD_PRIV_KEY));
    }

    public static String getResetPasswordPubKeyFile() {
        return WebConfig.getRuntimePath() + "/" + (String) CONFIG_CAHCE.get(WebConfigKey.SECURITY_RESET_PASSWD_PUB_KEY,
                key -> propertyReader.getString(key, WebConfigDefault.SECURITY_RESET_PASSWD_PUB_KEY));
    }

}
