/*
 * Copyright 2013-2019 the original author or authors.
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
package org.eulerframework.config;

import org.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import org.eulerframework.cache.inMemoryCache.ObjectCachePool;
import org.eulerframework.common.util.property.FilePropertySource;
import org.eulerframework.common.util.property.PropertyNotFoundException;
import org.eulerframework.common.util.property.PropertyReader;
import org.eulerframework.web.config.ProjectMode;
import org.eulerframework.web.config.RedisType;
import org.eulerframework.web.config.WebConfig;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class EulerWebSupportConfig {
    private static final DefaultObjectCache<String, Object> CONFIG_CAHCE = ObjectCachePool
            .generateDefaultObjectCache(Long.MAX_VALUE);

    private static PropertyReader propertyReader;

    static {
        try {
            propertyReader = new PropertyReader(new FilePropertySource("/config.properties"));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setPropertyReader(PropertyReader propertyReader) {
        EulerWebSupportConfig.propertyReader = propertyReader;
    }

    private static class EulerWebSupportConfigKeys {
        // [core]
        private static final String CORE_ROOT_CONTEXT_CONFIG_CLASS = "core.rootContextConfigClass";
        private static final String CORE_WEB_CONFIG_CLASS = "core.webConfigClass";
        private static final String CORE_AJAX_CONFIG_CLASS = "core.webAjaxConfigClass";
        private static final String CORE_ADMIN_WEB_CONFIG_CLASS = "core.adminWebConfigClass";
        private static final String CORE_ADMIN_AJAX_CONFIG_CLASS = "core.adminWebAjaxConfigClass";
        private static final String CORE_API_CONFIG_CLASS = "core.apiConfigClass";

        // [web]

        // [project]
        private static final String PROJECT_VERSION = "project.version";
        private static final String PROJECT_MODE = "project.mode";
        private static final String PROJECT_BUILD_TIME = "project.buildTime";
        private static final String PROJECT_COPYRIGHT_HOLDER = "project.copyrightHolder";

        // [Redis]
        private static final String REDIS_TYPE = "redis.type";
        private static final String REDIS_HOST = "redis.host";
        private static final String REDIS_PORT = "redis.port";
        private static final String REDIS_PASSWORD = "redis.password";
        private static final String REDIS_SENTINELS = "redis.sentinels";
    }

    private static class EulerWebSupportConfigDefault {
        private static final String CORE_ROOT_CONTEXT_CONFIG_CLASS = "org.eulerframework.config.root.RootContextConfig";
        private static final String CORE_WEB_CONFIG_CLASS = "org.eulerframework.config.controller.JspServletContextConfig";
        private static final String CORE_AJAX_CONFIG_CLASS = "org.eulerframework.config.controller.AjaxServletContextConfig";
        private static final String CORE_ADMIN_WEB_CONFIG_CLASS = "org.eulerframework.config.controller.AdminJspServletContextConfig";
        private static final String CORE_ADMIN_AJAX_CONFIG_CLASS = "org.eulerframework.config.controller.AdminAjaxServletContextConfig";
        private static final String CORE_API_CONFIG_CLASS = "org.eulerframework.config.controller.ApiServletContextConfig";

        private static final String PROJECT_COPYRIGHT_HOLDER = "Copyright Holder";
        private static final ProjectMode PROJECT_MODE = ProjectMode.DEBUG;

        // [Redis]
        private static final RedisType REDIS_TYPE = RedisType.STANDALONE;
        private static final String REDIS_HOST = "localhost";
        private static final int REDIS_PORT = 6379;
        private static final String REDIS_PASSWORD = null;
    }

    /**
     * 获取外部配置文件路径
     *
     * @return 外部配置文件路径
     */
    public static String getConfigPath() {
        return WebConfig.getRuntimePath() + "/conf/config.properties";
    }

    public static String getRootContextConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.CORE_ROOT_CONTEXT_CONFIG_CLASS, key -> propertyReader.getString(key, EulerWebSupportConfigDefault.CORE_ROOT_CONTEXT_CONFIG_CLASS));

        return (String) cachedConfig;
    }

    public static String getWebConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.CORE_WEB_CONFIG_CLASS, key -> propertyReader.getString(EulerWebSupportConfigKeys.CORE_WEB_CONFIG_CLASS, EulerWebSupportConfigDefault.CORE_WEB_CONFIG_CLASS));

        return (String) cachedConfig;
    }

    public static String getAjaxConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.CORE_AJAX_CONFIG_CLASS, key -> propertyReader.getString(EulerWebSupportConfigKeys.CORE_AJAX_CONFIG_CLASS, EulerWebSupportConfigDefault.CORE_AJAX_CONFIG_CLASS));

        return (String) cachedConfig;
    }

    public static String getAdminWebConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.CORE_ADMIN_WEB_CONFIG_CLASS,
                key -> propertyReader.getString(EulerWebSupportConfigKeys.CORE_ADMIN_WEB_CONFIG_CLASS,
                        EulerWebSupportConfigDefault.CORE_ADMIN_WEB_CONFIG_CLASS));

        return (String) cachedConfig;
    }

    public static String getAdminAjaxConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.CORE_ADMIN_AJAX_CONFIG_CLASS, key -> propertyReader.getString(EulerWebSupportConfigKeys.CORE_ADMIN_AJAX_CONFIG_CLASS, EulerWebSupportConfigDefault.CORE_ADMIN_AJAX_CONFIG_CLASS));

        return (String) cachedConfig;
    }

    public static String getApiConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.CORE_API_CONFIG_CLASS, key -> propertyReader.getString(EulerWebSupportConfigKeys.CORE_API_CONFIG_CLASS, EulerWebSupportConfigDefault.CORE_API_CONFIG_CLASS));

        return (String) cachedConfig;
    }

    public static ProjectMode getProjectMode() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.PROJECT_MODE, key -> propertyReader.getEnumValue(EulerWebSupportConfigKeys.PROJECT_MODE, EulerWebSupportConfigDefault.PROJECT_MODE, true));

        return (ProjectMode) cachedConfig;
    }

    public static String getProjectVersion() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.PROJECT_VERSION, key -> {
            try {
                return propertyReader.getString(EulerWebSupportConfigKeys.PROJECT_VERSION);
            } catch (PropertyNotFoundException e) {
                throw new RuntimeException("Couldn't load " + EulerWebSupportConfigKeys.PROJECT_VERSION);
            }
        });

        return (String) cachedConfig;
    }

    public static String getProjectBuildTime() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.PROJECT_BUILD_TIME, key -> {
            try {
                return propertyReader.getString(EulerWebSupportConfigKeys.PROJECT_BUILD_TIME);
            } catch (PropertyNotFoundException e) {
                throw new RuntimeException("Couldn't load " + EulerWebSupportConfigKeys.PROJECT_BUILD_TIME);
            }
        });

        return (String) cachedConfig;
    }

    public static String getCopyrightHolder() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.PROJECT_COPYRIGHT_HOLDER, key -> propertyReader.getString(EulerWebSupportConfigKeys.PROJECT_COPYRIGHT_HOLDER, EulerWebSupportConfigDefault.PROJECT_COPYRIGHT_HOLDER));

        return (String) cachedConfig;
    }

    public static RedisType getRedisType() {
        return (RedisType) CONFIG_CAHCE.get(EulerWebSupportConfigKeys.REDIS_TYPE,
                key -> propertyReader.getEnumValue(key, EulerWebSupportConfigDefault.REDIS_TYPE, true));
    }

    public static String getRedisHost() {
        return (String) CONFIG_CAHCE.get(EulerWebSupportConfigKeys.REDIS_HOST,
                key -> propertyReader.getString(key, EulerWebSupportConfigDefault.REDIS_HOST));
    }

    public static String getRedisPassword() {
        return (String) CONFIG_CAHCE.get(EulerWebSupportConfigKeys.REDIS_PASSWORD,
                key -> propertyReader.getString(key, EulerWebSupportConfigDefault.REDIS_PASSWORD));
    }

    public static int getRedisPort() {
        return (int) CONFIG_CAHCE.get(EulerWebSupportConfigKeys.REDIS_PORT,
                key -> propertyReader.getIntValue(key, EulerWebSupportConfigDefault.REDIS_PORT));
    }

    /**
     * @return 获取redis配置
     */
    public static String[] getRedisSentinels() {
        String str = (String) CONFIG_CAHCE.get(EulerWebSupportConfigKeys.REDIS_SENTINELS, key -> {
            try {
                return propertyReader.getString(key);
            } catch (PropertyNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        Assert.hasText(str, () -> EulerWebSupportConfigKeys.REDIS_SENTINELS + "can not be empty");

        return str.split(",");
    }
}
