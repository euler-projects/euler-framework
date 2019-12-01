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
package org.eulerframework.web.config;

import org.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import org.eulerframework.cache.inMemoryCache.ObjectCachePool;
import org.eulerframework.common.util.CommonUtils;
import org.eulerframework.common.util.StringUtils;
import org.eulerframework.common.util.property.FilePropertySource;
import org.eulerframework.common.util.property.PropertyNotFoundException;
import org.eulerframework.common.util.property.PropertyReader;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystemException;
import java.util.Locale;

public abstract class WebConfig {
    private static final DefaultObjectCache<String, Object> CONFIG_CACHE = ObjectCachePool
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
        WebConfig.propertyReader = propertyReader;
    }

    public static class WebConfigKey {
        // [core]
        /**
         * 应用名称
         */
        public static final String CORE_APPLICATION_NAME = "core.application.name";
        /**
         * 应用运行时目录
         */
        public static final String CORE_RUNTIME_PATH = "core.runtimePath";
        /**
         * 应用临时目录
         */
        public static final String CORE_TEMP_PATH = "core.tempPath";

        // [core.cache]
        /**
         * 内存缓存池清理周期, 毫秒
         */
        public static final String CORE_CACHE_RAM_CACHE_POOL_CLEAN_FREQ = "core.cache.ramCachePool.cleanFreq";

        // [web]
        /**
         * 后台管理页面图标文件存放位置
         * TODO: 支持存放在外部目录
         */
        public static final String WEB_ADMIN_DASHBOARD_BRAND_ICON = "web.admin.dashboardBrandIcon";
        /**
         * 后台管理页面标题文本
         */
        public static final String WEB_ADMIN_DASHBOARD_BRAND_TEXT = "web.admin.dashboardBrandText";
        /**
         * 后台管理JSP页面文件存放目录
         */
        public static final String WEB_ADMIN_JSP_PATH = "web.admin.jspPath";
        /**
         * 后台管理页面URL根路径
         */
        public static final String WEB_ADMIN_ROOT_PATH = "web.admin.rootPath";
        /**
         * 开启RESTful API支持
         */
        public static final String WEB_API_ENABLED = "web.api.enabled";
        /**
         * RESTful API URL根路径
         */
        public static final String WEB_API_ROOT_PATH = "web.api.rootPath";
        /**
         * 站点默认语言
         */
        public static final String WEB_LANGUAGE_DEFAULT = "web.language.default";
        /**
         * 站点支持语言列表
         */
        public static final String WEB_LANGUAGE_SUPPORT_LANGUAGES = "web.language.supportLanguages";
        /**
         * 静态资源文件URL根路径
         */
        public static final String WEB_SITE_ASSETS_PATH = "web.site.assetsPath";
        /**
         * 站点默认主题
         */
        public static final String WEB_SITE_DEFAULT_THEME = "web.site.defaultTheme";
        /**
         * 站点JSP文件存放目录
         */
        public static final String WEB_SITE_JSP_PATH = "web.site.jspPath";
        /**
         * 站点名称
         */
        public static final String WEB_SITE_NAME = "web.site.name";
        /**
         * 静态页面URL根路径
         */
        public static final String WEB_SITE_STATIC_PAGES_PATH = "web.site.staticPagesPath";
        /**
         * 站点URL
         */
        public static final String WEB_SITE_URL = "web.site.url";
        /**
         * 崩溃页面显示详细异常栈
         */
        public static final String WEB_JSP_SHOW_STACK_TRACE = "web.jsp.showStackTrace";
        //private static final String WEB_JSP_AUTO_DEPLOY_ENABLED = "web.jspAutoDeployEnabled";

        /**
         * Multipart request 配置数据缓存对象的KEY，并不用于配置文件
         */
        public static final String WEB_MULTIPART = "web.multipart";
        /**
         * the directory location where files will be stored
         */
        public static final String WEB_MULTIPART_LOCATION = "web.multiPart.location";
        /**
         * the maximum size allowed for uploaded files
         */
        public static final String WEB_MULTIPART_MAX_FILE_SIZE = "web.multiPart.maxFileSize";
        /**
         * the maximum size allowed for multipart/form-data requests
         */
        public static final String WEB_MULTIPART_MAX_REQUEST_SIZE = "web.multiPart.maxRequestSize";
        /**
         * the size threshold after which files will be written to disk
         */
        public static final String WEB_MULTIPART_FILE_SIZE_THRESHOLD = "web.multiPart.fileSizeThreshold";
    }

    public static class WebConfigDefault {
        private static final String CORE_APPLICATION_NAME = null;
        private static final String CORE_RUNTIME_PATH_PREFIX = "/var/run";
        private static final String CORE_TEMP_PATH_PREFIX = "/var/tmp";

        private static final long CORE_CACHE_RAM_CACHE_POOL_CLEAN_FREQ = 60_000L;

        private static final String WEB_ADMIN_DASHBOARD_BRAND_ICON = "/assets/system/admin-dashboard-brand.png";
        private static final String WEB_ADMIN_DASHBOARD_BRAND_TEXT = "Manage Dashboard";
        private static final String WEB_ADMIN_JSP_PATH = "/WEB-INF/jsp/admin/themes";
        private static final String WEB_ADMIN_ROOT_PATH = "/admin";
        private static final boolean WEB_API_ENABLED = true;
        private static final String WEB_API_ROOT_PATH = "/api";
        private static final Locale WEB_LANGUAGE_DEFAULT = Locale.CHINA;
        private static final Locale[] WEB_LANGUAGE_SUPPORT_LANGUAGES = new Locale[]{Locale.CHINA, Locale.US};
        private static final String WEB_SITE_ASSETS_PATH = "/assets";
        private static final String WEB_SITE_DEFAULT_THEME = "default";
        private static final String WEB_SITE_JSP_PATH = "/WEB-INF/jsp/themes";
        private static final String WEB_SITE_NAME = "DEMO";
        private static final String WEB_SITE_STATIC_PAGES_PATH = "/pages";
        public static final boolean WEB_JSP_SHOW_STACK_TRACE = false;
        //private static final boolean WEB_JSP_AUTO_DEPLOY_ENABLED = true;
        private static final String WEB_MULTIPART_LOCATION = null;
        private static final long WEB_MULTIPART_MAX_FILE_SIZE = 51_200L;
        private static final long WEB_MULTIPART_MAX_REQUEST_SIZE = 51_200L;
        private static final int WEB_MULTIPART_FILE_SIZE_THRESHOLD = 1_024;
    }

    public static String getApplicationName() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.CORE_APPLICATION_NAME, key -> propertyReader.get(WebConfigKey.CORE_APPLICATION_NAME, null));

        return cachedConfig == null ? null : (String) cachedConfig;
    }

    private static final String DEFAULT_APPLICATION_NAME = "euler-framework";

    public static String getRuntimePath() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.CORE_RUNTIME_PATH, key -> {
            String result = propertyReader.get(key, WebConfigDefault.CORE_APPLICATION_NAME);
            try {
                return ConfigUtils.handleApplicationPath(
                        result,
                        () -> {
                            String applicationName = getApplicationName();
                            return WebConfigDefault.CORE_RUNTIME_PATH_PREFIX + "/" + (StringUtils.hasText(applicationName) ? applicationName : DEFAULT_APPLICATION_NAME);
                        },
                        key);
            } catch (FileSystemException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });

        return (String) cachedConfig;
    }

    public static String getTempPath() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.CORE_TEMP_PATH, key -> {
            String result = propertyReader.get(key, null);
            try {
                return ConfigUtils.handleApplicationPath(
                        result,
                        () -> {
                            String applicationName = getApplicationName();
                            return WebConfigDefault.CORE_TEMP_PATH_PREFIX + "/" + (StringUtils.hasText(applicationName) ? applicationName : DEFAULT_APPLICATION_NAME);
                        },
                        key);
            } catch (FileSystemException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });

        return (String) cachedConfig;
    }

    /**
     * 获得站点默认语言
     *
     * @return 站点默认语言
     */
    public static Locale getDefaultLanguage() {
        return (Locale) CONFIG_CACHE.get(WebConfigKey.WEB_LANGUAGE_DEFAULT, key -> {
            try {
                String defaultLanguagesStr = propertyReader.get(key);
                Assert.hasText(defaultLanguagesStr, WebConfigKey.WEB_LANGUAGE_DEFAULT + " can not be empty");
                return CommonUtils.parseLocale(defaultLanguagesStr);
            } catch (PropertyNotFoundException e) {
                return WebConfigDefault.WEB_LANGUAGE_DEFAULT;
            }
        });
    }

    public static Locale[] getSupportLanguages() {
        return (Locale[]) CONFIG_CACHE.get(WebConfigKey.WEB_LANGUAGE_SUPPORT_LANGUAGES, key -> {
            try {
                String supportLanguagesStr = propertyReader.get(key);
                String[] supportLanguagesStrArray = supportLanguagesStr.split(",");
                Locale[] ret = new Locale[supportLanguagesStrArray.length];

                for (int i = 0; i < supportLanguagesStrArray.length; i++) {
                    ret[i] = CommonUtils.parseLocale(supportLanguagesStrArray[i]);
                }
                return ret;
            } catch (PropertyNotFoundException e) {
                return WebConfigDefault.WEB_LANGUAGE_SUPPORT_LANGUAGES;
            }
        });
    }


    public static boolean isApiEnabled() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.WEB_API_ENABLED, key -> propertyReader.getBooleanValue(key, WebConfigDefault.WEB_API_ENABLED));

        return (boolean) cachedConfig;
    }

    /**
     * 获取网站域名
     *
     * @return 末尾不带/的网站域名
     * <p> 如<br>
     * http://localhost:8080<br>
     * http://123.123.123.123:1234<br>
     * http://123.123.123.123<br>
     * https://eulerproject.io
     */
    public static String getWebUrl() {
        try {
            String result = propertyReader.get(WebConfigKey.WEB_SITE_URL);
            if (!StringUtils.hasText(result))
                throw new RuntimeException(WebConfigKey.WEB_SITE_URL + "can not be empty");

            while (result.endsWith("/")) {
                result = result.substring(0, result.length() - 1);
            }

            return result;
        } catch (PropertyNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAdminRootPath() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.WEB_ADMIN_ROOT_PATH, key -> {

            String result = propertyReader.get(WebConfigKey.WEB_ADMIN_ROOT_PATH, WebConfigDefault.WEB_ADMIN_ROOT_PATH);

            if (!StringUtils.hasText(result))
                throw new RuntimeException(WebConfigKey.WEB_ADMIN_ROOT_PATH + " can not be empty");

            while (result.endsWith("*")) {
                result = result.substring(0, result.length() - 1);
            }

            result = CommonUtils.convertDirToUnixFormat(result, false);

            if (!result.startsWith("/"))
                result = "/" + result;

            return result;
        });

        return (String) cachedConfig;
    }

    public static String getStaticPagesRootPath() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.WEB_SITE_STATIC_PAGES_PATH, key -> {

            String result = propertyReader.get(key, WebConfigDefault.WEB_SITE_STATIC_PAGES_PATH);

            if (!StringUtils.hasText(result))
                throw new RuntimeException(key + " can not be empty");

            while (result.endsWith("*")) {
                result = result.substring(0, result.length() - 1);
            }

            result = CommonUtils.convertDirToUnixFormat(result, false);

            if (!result.startsWith("/"))
                result = "/" + result;

            return result;
        });

        return (String) cachedConfig;
    }

    public static String getJspPath() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.WEB_SITE_JSP_PATH, key -> CommonUtils.convertDirToUnixFormat(
                propertyReader.get(WebConfigKey.WEB_SITE_JSP_PATH, WebConfigDefault.WEB_SITE_JSP_PATH), true));

        return (String) cachedConfig;
    }

    public static String getAdminJspPath() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.WEB_ADMIN_JSP_PATH, key -> CommonUtils.convertDirToUnixFormat(
                propertyReader.get(key, WebConfigDefault.WEB_ADMIN_JSP_PATH), true));

        return (String) cachedConfig;
    }

    public static String getApiRootPath() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.WEB_API_ROOT_PATH, key -> {
            String result = propertyReader.get(key, WebConfigDefault.WEB_API_ROOT_PATH);

            if (!StringUtils.hasText(result))
                throw new RuntimeException(key + "can not be empty");

            while (result.endsWith("*")) {
                result = result.substring(0, result.length() - 1);
            }

            result = CommonUtils.convertDirToUnixFormat(result, false);

            if (!result.startsWith("/"))
                result = "/" + result;

            return result;
        });

        return (String) cachedConfig;
    }

    public static String getSiteName() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.WEB_SITE_NAME, key -> propertyReader.get(WebConfigKey.WEB_SITE_NAME, WebConfigDefault.WEB_SITE_NAME));

        return (String) cachedConfig;
    }

    public static String getAssetsPath() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.WEB_SITE_ASSETS_PATH, key -> propertyReader.get(WebConfigKey.WEB_SITE_ASSETS_PATH, WebConfigDefault.WEB_SITE_ASSETS_PATH));

        return (String) cachedConfig;
    }

    public static String getDefaultTheme() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.WEB_SITE_DEFAULT_THEME, key -> propertyReader.get(WebConfigKey.WEB_SITE_DEFAULT_THEME, WebConfigDefault.WEB_SITE_DEFAULT_THEME));

        return (String) cachedConfig;
    }

    public static String getAdminDashboardBrandIcon() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_ICON,
                key -> propertyReader.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_ICON,
                        WebConfigDefault.WEB_ADMIN_DASHBOARD_BRAND_ICON));

        return (String) cachedConfig;
    }

    public static String getAdminDashboardBrandText() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_TEXT,
                key -> propertyReader.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_TEXT,
                        WebConfigDefault.WEB_ADMIN_DASHBOARD_BRAND_TEXT));

        return (String) cachedConfig;
    }


    public static long getRamCacheCleanFreq() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.CORE_CACHE_RAM_CACHE_POOL_CLEAN_FREQ,
                key -> propertyReader.getLongValue(WebConfigKey.CORE_CACHE_RAM_CACHE_POOL_CLEAN_FREQ,
                        WebConfigDefault.CORE_CACHE_RAM_CACHE_POOL_CLEAN_FREQ));

        return (long) cachedConfig;
    }

    public static boolean showStackTraceInCrashPage() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.WEB_JSP_SHOW_STACK_TRACE,
                key -> propertyReader.getBooleanValue(WebConfigKey.WEB_JSP_SHOW_STACK_TRACE,
                        WebConfigDefault.WEB_JSP_SHOW_STACK_TRACE));

        return (boolean) cachedConfig;
    }

    public static MultiPartConfig getMultiPartConfig() {
        Object cachedConfig = CONFIG_CACHE.get(WebConfigKey.WEB_MULTIPART, key -> {
            String location = propertyReader.get(WebConfigKey.WEB_MULTIPART_LOCATION,
                    WebConfigDefault.WEB_MULTIPART_LOCATION);
            long maxFileSize = propertyReader.getLongValue(WebConfigKey.WEB_MULTIPART_MAX_FILE_SIZE,
                    WebConfigDefault.WEB_MULTIPART_MAX_FILE_SIZE);
            long maxRequestSize = propertyReader.getLongValue(WebConfigKey.WEB_MULTIPART_MAX_REQUEST_SIZE,
                    WebConfigDefault.WEB_MULTIPART_MAX_REQUEST_SIZE);
            int fileSizeThreshold = propertyReader.getIntValue(WebConfigKey.WEB_MULTIPART_FILE_SIZE_THRESHOLD,
                    WebConfigDefault.WEB_MULTIPART_FILE_SIZE_THRESHOLD);

            return new MultiPartConfig(location, maxFileSize, maxRequestSize, fileSizeThreshold);
        });

        return (MultiPartConfig) cachedConfig;
    }
}
