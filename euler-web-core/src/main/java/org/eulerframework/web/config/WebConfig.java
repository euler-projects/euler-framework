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
        WebConfig.propertyReader = propertyReader;
    }

    public static class WebConfigKey {
        // [core]
        public static final String CORE_APPLICATION_NAME = "core.application.name";
        public static final String CORE_RUNTIME_PATH = "core.runtimePath";
        public static final String CORE_TEMP_PATH = "core.tempPath";
        private static final String CORE_CACHE_I18N_REFRESH_FREQ = "core.cache.i18n.refreshFreq";
        private static final String CORE_CACHE_RAM_CACHE_POOL_CLEAN_FREQ = "core.cache.ramCachePool.cleanFreq";

        // [web]
        private static final String WEB_API_ENABLED = "web.api.enabled";
        private static final String WEB_DEFAULT_LANGUAGE = "web.defaultLanguage";
        private static final String WEB_SUPPORT_LANGUAGES = "web.supportLanguages";
        private static final String WEB_URL = "web.url";
        private static final String WEB_SITE_NAME = "web.siteName";
        private static final String WEB_DEFAULT_THEME = "web.defaultTheme";
        private static final String WEB_STATIC_PAGES_PATH = "web.staticPagesPath";
        private static final String WEB_JSP_PATH = "web.jspPath";
        //private static final String WEB_JSP_AUTO_DEPLOY_ENABLED = "web.jspAutoDeployEnabled";
        private static final String WEB_ADMIN_JSP_PATH = "web.admin.JspPath";
        private static final String WEB_ADMIN_ROOT_PATH = "web.admin.rootPath";
        private static final String WEB_API_ROOT_PATH = "web.api.rootPath";
        private static final String WEB_ASSETS_PATH = "web.asstesPath";
        private static final String WEB_ADMIN_DASHBOARD_BRAND_ICON = "web.admin.dashboardBrandIcon";
        private static final String WEB_ADMIN_DASHBOARD_BRAND_TEXT = "web.admin.dashboardBrandText";
        private static final String WEB_MULTIPART = "web.multipart";
        private static final String WEB_MULTIPART_LOCATION = "web.multiPart.location";
        private static final String WEB_MULTIPART_MAX_FILE_SIZE = "web.multiPart.maxFileSize";
        private static final String WEB_MULTIPART_MAX_REQUEST_SIZE = "web.multiPart.maxRequestSize";
        private static final String WEB_MULTIPART_FILE_SIZE_THRESHOLD = "web.multiPart.fileSizeThreshold";

        // [project]
        private static final String PROJECT_VERSION = "project.version";
        private static final String PROJECT_MODE = "project.mode";
        private static final String PROJECT_BUILDTIME = "project.buildtime";
        private static final String PROJECT_COPYRIGHT_HOLDER = "project.copyrightHolder";

        // [Redis]
        private static final String REDIS_TYPE = "redis.type";
        private static final String REDIS_HOST = "redis.host";
        private static final String REDIS_PORT = "redis.port";
        private static final String REDIS_PASSWORD = "redis.password";
        private static final String REDIS_SENTINELS = "redis.sentinels";
    }

    public static class WebConfigDefault {
        private static final String CORE_APPLICATION_NAME = "euler-framework";
        private static final String CORE_RUNTIME_PATH_PREFIX = "/var/run";
        private static final String CORE_TEMP_PATH_PREFIX = "/var/tmp";
        private static final int CORE_CACHE_I18N_REFRESH_FREQ = 86_400;
        private static final long CORE_CACHE_RAM_CACHE_POOL_CLEAN_FREQ = 60_000L;

        private static final boolean WEB_API_ENABLED = true;
        private static final Locale WEB_DEFAULT_LANGUAGE = Locale.CHINA;
        private static final Locale[] WEB_SUPPORT_LANGUAGES = new Locale[]{Locale.CHINA, Locale.US};
        private static final String WEB_SITE_NAME = "DEMO";
        private static final String WEB_DEFAULT_THEME = "default";
        //private static final boolean WEB_JSP_AUTO_DEPLOY_ENABLED = true;
        private static final String WEB_STATIC_PAGES_PATH = "/pages";
        private static final String WEB_JSP_PATH = "/WEB-INF/jsp/themes";
        private static final String WEB_ADMIN_JSP_PATH = "/WEB-INF/jsp/admin/themes";
        private static final String WEB_ADMIN_ROOT_PATH = "/admin";
        private static final String WEB_API_ROOT_PATH = "/api";
        private static final String WEB_ASSETS_PATH = "/assets";
        private static final String WEB_ADMIN_DASHBOARD_BRAND_ICON = "/assets/system/admin-dashboard-brand.png";
        private static final String WEB_ADMIN_DASHBOARD_BRAND_TEXT = "Manage Dashboard";
        private static final String WEB_MULTIPART_LOCATION = null;
        private static final long WEB_MULTIPART_MAX_FILE_SIZE = 51_200L;
        private static final long WEB_MULTIPART_MAX_REQUEST_SIZE = 51_200L;
        private static final int WEB_MULTIPART_FILE_SIZE_THRESHOLD = 1_024;

        private static final String PROJECT_COPYRIGHT_HOLDER = "Copyright Holder";
        private static final ProjectMode PROJECT_MODE = ProjectMode.DEBUG;

        // [Redis]
        private static final RedisType REDIS_TYPE = RedisType.STANDALONE;
        private static final String REDIS_HOST = "localhost";
        private static final int REDIS_PORT = 6379;
        private static final String REDIS_PASSWORD = null;
    }

    public static String getApplicationName() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_APPLICATION_NAME, key -> propertyReader.get(WebConfigKey.CORE_APPLICATION_NAME,
                WebConfigDefault.CORE_APPLICATION_NAME));

        return (String) cachedConfig;
    }

    public static String getRuntimePath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_RUNTIME_PATH, key -> {
            String result = propertyReader.get(key, null);
            try {
                return ConfigUtils.handleApplicationPath(result, () -> WebConfigDefault.CORE_RUNTIME_PATH_PREFIX + "/" + getApplicationName(), key);
            } catch (FileSystemException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });

        return (String) cachedConfig;
    }

    public static String getTempPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_TEMP_PATH, key -> {
            String result = propertyReader.get(key, null);
            try {
                return ConfigUtils.handleApplicationPath(result, () -> WebConfigDefault.CORE_TEMP_PATH_PREFIX + "/" + getApplicationName(), key);
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
        return (Locale) CONFIG_CAHCE.get(WebConfigKey.WEB_DEFAULT_LANGUAGE, key -> {
            try {
                String defaultLanguagesStr = propertyReader.get(key);
                Assert.hasText(defaultLanguagesStr, WebConfigKey.WEB_DEFAULT_LANGUAGE + " can not be empty");
                return CommonUtils.parseLocale(defaultLanguagesStr);
            } catch (PropertyNotFoundException e) {
                return WebConfigDefault.WEB_DEFAULT_LANGUAGE;
            }
        });
    }

    public static Locale[] getSupportLanguages() {
        return (Locale[]) CONFIG_CAHCE.get(WebConfigKey.WEB_SUPPORT_LANGUAGES, key -> {
            try {
                String supportLanguagesStr = propertyReader.get(key);
                String[] supportLanguagesStrArray = supportLanguagesStr.split(",");
                Locale[] ret = new Locale[supportLanguagesStrArray.length];

                for (int i = 0; i < supportLanguagesStrArray.length; i++) {
                    ret[i] = CommonUtils.parseLocale(supportLanguagesStrArray[i]);
                }
                return ret;
            } catch (PropertyNotFoundException e) {
                return WebConfigDefault.WEB_SUPPORT_LANGUAGES;
            }
        });
    }


    public static boolean isApiEnabled() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_API_ENABLED, key -> propertyReader.getBooleanValue(key, WebConfigDefault.WEB_API_ENABLED));

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
            String result = propertyReader.get(WebConfigKey.WEB_URL);
            if (!StringUtils.hasText(result))
                throw new RuntimeException(WebConfigKey.WEB_URL + "can not be empty");

            while (result.endsWith("/")) {
                result = result.substring(0, result.length() - 1);
            }

            return result;
        } catch (PropertyNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAdminRootPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ADMIN_ROOT_PATH, key -> {

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
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_STATIC_PAGES_PATH, key -> {

            String result = propertyReader.get(key, WebConfigDefault.WEB_STATIC_PAGES_PATH);

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
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_JSP_PATH, key -> CommonUtils.convertDirToUnixFormat(
                propertyReader.get(WebConfigKey.WEB_JSP_PATH, WebConfigDefault.WEB_JSP_PATH), true));

        return (String) cachedConfig;
    }

    public static String getAdminJspPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ADMIN_JSP_PATH, key -> CommonUtils.convertDirToUnixFormat(
                propertyReader.get(key, WebConfigDefault.WEB_ADMIN_JSP_PATH), true));

        return (String) cachedConfig;
    }

    public static String getApiRootPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_API_ROOT_PATH, key -> {
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
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_SITE_NAME, key -> propertyReader.get(WebConfigKey.WEB_SITE_NAME, WebConfigDefault.WEB_SITE_NAME));

        return (String) cachedConfig;
    }

    public static String getAssetsPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ASSETS_PATH, key -> propertyReader.get(WebConfigKey.WEB_ASSETS_PATH, WebConfigDefault.WEB_ASSETS_PATH));

        return (String) cachedConfig;
    }

    public static String getDefaultTheme() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_DEFAULT_THEME, key -> propertyReader.get(WebConfigKey.WEB_DEFAULT_THEME, WebConfigDefault.WEB_DEFAULT_THEME));

        return (String) cachedConfig;
    }

    public static String getAdminDashboardBrandIcon() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_ICON,
                key -> propertyReader.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_ICON,
                        WebConfigDefault.WEB_ADMIN_DASHBOARD_BRAND_ICON));

        return (String) cachedConfig;
    }

    public static String getAdminDashboardBrandText() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_TEXT,
                key -> propertyReader.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_TEXT,
                        WebConfigDefault.WEB_ADMIN_DASHBOARD_BRAND_TEXT));

        return (String) cachedConfig;
    }

    public static int getI18nRefreshFreq() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_CACHE_I18N_REFRESH_FREQ, key -> propertyReader.getIntValue(WebConfigKey.CORE_CACHE_I18N_REFRESH_FREQ,
                WebConfigDefault.CORE_CACHE_I18N_REFRESH_FREQ));

        return (int) cachedConfig;
    }


    public static long getRamCacheCleanFreq() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_CACHE_RAM_CACHE_POOL_CLEAN_FREQ,
                key -> propertyReader.getLongValue(WebConfigKey.CORE_CACHE_RAM_CACHE_POOL_CLEAN_FREQ,
                        WebConfigDefault.CORE_CACHE_RAM_CACHE_POOL_CLEAN_FREQ));

        return (long) cachedConfig;
    }

    public static ProjectMode getProjectMode() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.PROJECT_MODE, key -> propertyReader.getEnumValue(WebConfigKey.PROJECT_MODE, WebConfigDefault.PROJECT_MODE, true));

        return (ProjectMode) cachedConfig;
    }

    public static String getProjectVersion() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.PROJECT_VERSION, key -> {
            try {
                return propertyReader.get(WebConfigKey.PROJECT_VERSION);
            } catch (PropertyNotFoundException e) {
                throw new RuntimeException("Couldn't load " + WebConfigKey.PROJECT_VERSION);
            }
        });

        return (String) cachedConfig;
    }

    public static String getProjectBuildtime() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.PROJECT_BUILDTIME, key -> {
            try {
                return propertyReader.get(WebConfigKey.PROJECT_BUILDTIME);
            } catch (PropertyNotFoundException e) {
                throw new RuntimeException("Couldn't load " + WebConfigKey.PROJECT_BUILDTIME);
            }
        });

        return (String) cachedConfig;
    }

    public static String getCopyrightHolder() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.PROJECT_COPYRIGHT_HOLDER, key -> propertyReader.get(WebConfigKey.PROJECT_COPYRIGHT_HOLDER, WebConfigDefault.PROJECT_COPYRIGHT_HOLDER));

        return (String) cachedConfig;
    }

    /**
     * 检查当前配置是不是调试模式<br>
     * <b>注意:</b>
     * 根据配置不同,调试模式可能包含多个{@link ProjectMode},并不是{@link ProjectMode#DEVELOP}
     *
     * @return 调试模式
     */
    public static boolean isDebugMode() {
        // TODO: make logdetailsmode configable
        return getProjectMode().equals(ProjectMode.DEVELOP) || getProjectMode().equals(ProjectMode.DEBUG);
    }

    /**
     * 获取外部配置文件路径
     *
     * @return 外部配置文件路径
     */
    public static String getConfigPath() {
        return getRuntimePath() + "/conf/config.properties";
    }

    public static RedisType getRedisType() {
        return (RedisType) CONFIG_CAHCE.get(WebConfigKey.REDIS_TYPE,
                key -> propertyReader.getEnumValue(key, WebConfigDefault.REDIS_TYPE, true));
    }

    public static String getRedisHost() {
        return (String) CONFIG_CAHCE.get(WebConfigKey.REDIS_HOST,
                key -> propertyReader.get(key, WebConfigDefault.REDIS_HOST));
    }

    public static String getRedisPassword() {
        return (String) CONFIG_CAHCE.get(WebConfigKey.REDIS_PASSWORD,
                key -> propertyReader.get(key, WebConfigDefault.REDIS_PASSWORD));
    }

    public static int getRedisPort() {
        return (int) CONFIG_CAHCE.get(WebConfigKey.REDIS_PORT,
                key -> propertyReader.getIntValue(key, WebConfigDefault.REDIS_PORT));
    }

    /**
     * @return 获取redis配置
     */
    public static String[] getRedisSentinels() {
        String str = (String) CONFIG_CAHCE.get(WebConfigKey.REDIS_SENTINELS, key -> {
            try {
                return propertyReader.get(key);
            } catch (PropertyNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        Assert.hasText(str, () -> WebConfigKey.REDIS_SENTINELS + "can not be empty");

        return str.split(",");
    }

    public static MultiPartConfig getMultiPartConfig() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_MULTIPART, key -> {
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
