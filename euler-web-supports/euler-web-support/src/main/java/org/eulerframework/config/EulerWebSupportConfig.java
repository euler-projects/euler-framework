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
import org.eulerframework.common.util.property.PropertyReader;

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
    }

    private static class EulerWebSupportConfigDefault {
        private static final String CORE_ROOT_CONTEXT_CONFIG_CLASS = "org.eulerframework.config.root.RootContextConfig";
        private static final String CORE_WEB_CONFIG_CLASS = "org.eulerframework.config.controller.JspServletContextConfig";
        private static final String CORE_AJAX_CONFIG_CLASS = "org.eulerframework.config.controller.AjaxServletContextConfig";
        private static final String CORE_ADMIN_WEB_CONFIG_CLASS = "org.eulerframework.config.controller.AdminJspServletContextConfig";
        private static final String CORE_ADMIN_AJAX_CONFIG_CLASS = "org.eulerframework.config.controller.AdminAjaxServletContextConfig";
        private static final String CORE_API_CONFIG_CLASS = "org.eulerframework.config.controller.ApiServletContextConfig";
    }

    public static String getRootContextConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.CORE_ROOT_CONTEXT_CONFIG_CLASS, key -> propertyReader.get(key, EulerWebSupportConfigDefault.CORE_ROOT_CONTEXT_CONFIG_CLASS));

        return (String) cachedConfig;
    }

    public static String getWebConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.CORE_WEB_CONFIG_CLASS, key -> propertyReader.get(EulerWebSupportConfigKeys.CORE_WEB_CONFIG_CLASS, EulerWebSupportConfigDefault.CORE_WEB_CONFIG_CLASS));

        return (String) cachedConfig;
    }

    public static String getAjaxConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.CORE_AJAX_CONFIG_CLASS, key -> propertyReader.get(EulerWebSupportConfigKeys.CORE_AJAX_CONFIG_CLASS, EulerWebSupportConfigDefault.CORE_AJAX_CONFIG_CLASS));

        return (String) cachedConfig;
    }

    public static String getAdminWebConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.CORE_ADMIN_WEB_CONFIG_CLASS,
                key -> propertyReader.get(EulerWebSupportConfigKeys.CORE_ADMIN_WEB_CONFIG_CLASS,
                        EulerWebSupportConfigDefault.CORE_ADMIN_WEB_CONFIG_CLASS));

        return (String) cachedConfig;
    }

    public static String getAdminAjaxConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.CORE_ADMIN_AJAX_CONFIG_CLASS, key -> propertyReader.get(EulerWebSupportConfigKeys.CORE_ADMIN_AJAX_CONFIG_CLASS, EulerWebSupportConfigDefault.CORE_ADMIN_AJAX_CONFIG_CLASS));

        return (String) cachedConfig;
    }

    public static String getApiConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(EulerWebSupportConfigKeys.CORE_API_CONFIG_CLASS, key -> propertyReader.get(EulerWebSupportConfigKeys.CORE_API_CONFIG_CLASS, EulerWebSupportConfigDefault.CORE_API_CONFIG_CLASS));

        return (String) cachedConfig;
    }
}
