package net.eulerframework.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ContextLoader;

import net.eulerframework.cache.inMemoryCache.AbstractObjectCache.DataGetter;
import net.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import net.eulerframework.cache.inMemoryCache.ObjectCachePool;
import net.eulerframework.common.util.CommonUtils;
import net.eulerframework.common.util.StringUtils;
import net.eulerframework.common.util.property.PropertyNotFoundException;
import net.eulerframework.common.util.property.PropertyReader;

@Configuration
public abstract class WebConfig {
    protected static final Logger LOGGER = LoggerFactory.getLogger(WebConfig.class);

    private static final DefaultObjectCache<String, Object> CONFIG_CAHCE = ObjectCachePool
            .generateDefaultObjectCache(Long.MAX_VALUE);

    private static final PropertyReader properties = new PropertyReader("/config.properties");

    private static class WebConfigKey {
        // [project]
        private static final String PROJECT_VERSION = "project.verison";
        private static final String PROJECT_MODE = "project.mode";
        private static final String PROJECT_BUILDTIME = "project.buildtime";
        private static final String PROJECT_COPYRIGHT_HOLDER = "project.copyrightHolder";

        // [core]
        private static final String CORE_ROOT_CONTEXT_CONFIG_CLASS = "core.rootContextConfigClass";
        private static final String CORE_WEB_CONFIG_CLASS = "core.webConfigClass";
        private static final String CORE_AJAX_CONFIG_CLASS = "core.webAjaxConfigClass";
        private static final String CORE_ADMIN_WEB_CONFIG_CLASS = "core.adminWebConfigClass";
        private static final String CORE_ADMIN_AJAX_CONFIG_CLASS = "core.adminWebAjaxConfigClass";
        private static final String CORE_API_CONFIG_CLASS = "core.apiConfigClass";
        private static final String CORE_CACHE_I18N_REFRESH_FREQ = "core.cache.i18n.refreshFreq";
        private static final String CORE_CAHCE_RAMCACHE_POOL_CLEAN_FREQ = "core.cache.ramCachePool.cleanFreq";

        // [web]
        private static final String WEB_SITENAME = "web.sitename";
        private static final String WEB_DEFAULT_THEME = "web.defaultTheme";
        private static final String WEB_FILE_SAVE_PATH = "web.fileSavePath";
        private static final String WEB_STATIC_PAGES_PATH = "web.staticPagesPath";
        private static final String WEB_JSP_PATH = "web.jspPath";
        //private static final String WEB_JSP_AUTO_DEPLOY_ENABLED = "web.jspAutoDeployEnabled";
        private static final String WEB_ADMIN_JSP_PATH = "web.admin.JspPath";
        private static final String WEB_ADMIN_ROOT_PATH = "web.admin.rootPath";
        private static final String WEB_ADMIN_DASHBOARD_BRAND_ICON = "web.admin.dashboardBrandIcon";
        private static final String WEB_ADMIN_DASHBOARD_BRAND_TEXT = "web.admin.dashboardBrandText";

        private static final String WEB_API_ENABLED = "web.api.enabled";
        private static final String WEB_API_ROOT_PATH = "web.api.rootPath";
        private static final String WEB_ASSETS_PATH = "web.asstesPath";
        public static final String WEB_DEFAULT_LANGUAGE = "web.defaultLanguage";

        private static final String WEB_MULITPART = "web.multipart";
        private static final String WEB_MULITPART_LOCATION = "web.multiPart.location";
        private static final String WEB_MULITPART_MAX_FILE_SIZE = "web.multiPart.maxFileSize";
        private static final String WEB_MULITPART_MAX_REQUEST_SIZE = "web.multiPart.maxRequestSize";
        private static final String WEB_MULITPART_FILE_SIZE_THRESHOLD = "web.multiPart.fileSizeThreshold";
    }

    private static class WebConfigDefault {
        private static final String PROJECT_COPYRIGHT_HOLDER = "Copyright Holder";
        private static final ProjectMode PROJECT_MODE = ProjectMode.DEBUG;

        private static final String CORE_ROOT_CONTEXT_CONFIG_CLASS = "net.eulerframework.config.root.RootContextConfig";
        private static final String CORE_WEB_CONFIG_CLASS = "net.eulerframework.config.controller.JspServletContextConfig";
        private static final String CORE_AJAX_CONFIG_CLASS = "net.eulerframework.config.controller.AjaxServletContextConfig";
        private static final String CORE_ADMIN_WEB_CONFIG_CLASS = "net.eulerframework.config.controller.AdminJspServletContextConfig";
        private static final String CORE_ADMIN_AJAX_CONFIG_CLASS = "net.eulerframework.config.controller.AdminAjaxServletContextConfig";
        private static final String CORE_API_CONFIG_CLASS = "net.eulerframework.config.controller.ApiServletContextConfig";
        private static final int CORE_CACHE_I18N_REFRESH_FREQ = 86_400;
        private static final long CORE_CAHCE_RAMCACHE_POOL_CLEAN_FREQ = 60_000L;

        private static final String WEB_SITENAME = "DEMO";
        private static final String WEB_DEFAULT_THEME = "default";
        private static final String WEB_FILE_SAVE_PATH_UNIX = "file:///var/lib/euler-framework/";
        private static final String WEB_FILE_SAVE_PATH_WIN = "file://C:\\euler-framework\\";
        //private static final boolean WEB_JSP_AUTO_DEPLOY_ENABLED = true;
        private static final String WEB_STATIC_PAGES_PATH = "/pages";
        private static final String WEB_JSP_PATH = "/WEB-INF/jsp/themes";
        private static final String WEB_ADMIN_JSP_PATH = "/WEB-INF/jsp/admin/themes";
        private static final String WEB_ADMIN_ROOT_PATH = "/admin";
        private static final String WEB_ADMIN_DASHBOARD_BRAND_ICON = "/assets/system/admin-dashboard-brand.png";
        private static final String WEB_ADMIN_DASHBOARD_BRAND_TEXT = "Manage Dashboard";
        
        private static final boolean WEB_API_ENABLED = true;
        private static final String WEB_API_ROOT_PATH = "/api";
        private static final String WEB_ASSETS_PATH = "/assets";

        private static final String WEB_MULITPART_LOCATION = null;
        private static final long WEB_MULITPART_MAX_FILE_SIZE = 51_200L;
        private static final long WEB_MULITPART_MAX_REQUEST_SIZE = 51_200L;
        private static final int WEB_MULITPART_FILE_SIZE_THRESHOLD = 1_024;
    }

    public static boolean clearWebConfigCache() {
        properties.refresh();
        return CONFIG_CAHCE.clear();
    }

    public static int getI18nRefreshFreq() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_CACHE_I18N_REFRESH_FREQ, key -> {
            return properties.getIntValue(WebConfigKey.CORE_CACHE_I18N_REFRESH_FREQ,
                    WebConfigDefault.CORE_CACHE_I18N_REFRESH_FREQ);
        });

        return (int) cachedConfig;
    }

    public static boolean isApiEnabled() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_API_ENABLED, key -> {
            return properties.getBooleanValue(key, WebConfigDefault.WEB_API_ENABLED);
        });
        
        return (boolean) cachedConfig;
    }
    
    public static String getApiRootPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_API_ROOT_PATH, key -> {
            String result = properties.get(key, WebConfigDefault.WEB_API_ROOT_PATH);

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

    public static String getAdminRootPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ADMIN_ROOT_PATH, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {

                String result = properties.get(WebConfigKey.WEB_ADMIN_ROOT_PATH, WebConfigDefault.WEB_ADMIN_ROOT_PATH);

                if (!StringUtils.hasText(result))
                    throw new RuntimeException(WebConfigKey.WEB_ADMIN_ROOT_PATH + " can not be empty");

                while (result.endsWith("*")) {
                    result = result.substring(0, result.length() - 1);
                }

                result = CommonUtils.convertDirToUnixFormat(result, false);

                if (!result.startsWith("/"))
                    result = "/" + result;

                return result;
            }
        });

        return (String) cachedConfig;
    }
    
    public static String getStaticPagesRootPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_STATIC_PAGES_PATH, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {

                String result = properties.get(key, WebConfigDefault.WEB_STATIC_PAGES_PATH);

                if (!StringUtils.hasText(result))
                    throw new RuntimeException(key + " can not be empty");

                while (result.endsWith("*")) {
                    result = result.substring(0, result.length() - 1);
                }

                result = CommonUtils.convertDirToUnixFormat(result, false);

                if (!result.startsWith("/"))
                    result = "/" + result;

                return result;
            }
        });

        return (String) cachedConfig;
    }

    public static String getJspPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_JSP_PATH, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {

                return CommonUtils.convertDirToUnixFormat(
                        properties.get(WebConfigKey.WEB_JSP_PATH, WebConfigDefault.WEB_JSP_PATH), true);
            }

        });

        return (String) cachedConfig;
    }

    public static String getAdminJspPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ADMIN_JSP_PATH, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {

                return CommonUtils.convertDirToUnixFormat(
                        properties.get(WebConfigKey.WEB_ADMIN_JSP_PATH, WebConfigDefault.WEB_ADMIN_JSP_PATH), true);
            }

        });

        return (String) cachedConfig;
    }

    public static String getFileSavePath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_FILE_SAVE_PATH, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {

                String result;
                try {
                    result = properties.get(WebConfigKey.WEB_FILE_SAVE_PATH);
                } catch (PropertyNotFoundException e) {
                    if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
                        LOGGER.info("OS is windows");
                        result = WebConfigDefault.WEB_FILE_SAVE_PATH_WIN;
                    } else {
                        LOGGER.info("OS isn't windows");
                        result = WebConfigDefault.WEB_FILE_SAVE_PATH_UNIX;
                    }
                    LOGGER.warn("Couldn't load " + WebConfigKey.WEB_FILE_SAVE_PATH + " , use " + result + " for default.");
                }

                result = CommonUtils.convertDirToUnixFormat(result, true);
                if (!result.startsWith("/") && !result.startsWith("file://")) {
                    result = ContextLoader.getCurrentWebApplicationContext().getServletContext().getRealPath(result);
                } else {
                    if (result.startsWith("file://")) {
                        result = result.substring("file://".length());
                    }
                }

                return result;
            }

        });

        return (String) cachedConfig;
    }

    public static long getRamCacheCleanFreq() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_CAHCE_RAMCACHE_POOL_CLEAN_FREQ,
                new DataGetter<String, Object>() {

                    @Override
                    public Object getData(String key) {
                        return properties.getLongValue(WebConfigKey.CORE_CAHCE_RAMCACHE_POOL_CLEAN_FREQ,
                                WebConfigDefault.CORE_CAHCE_RAMCACHE_POOL_CLEAN_FREQ);
                    }

                });

        return (long) cachedConfig;
    }

    public static MultiPartConfig getMultiPartConfig() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_MULITPART, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {
                String location = properties.get(WebConfigKey.WEB_MULITPART_LOCATION,
                        WebConfigDefault.WEB_MULITPART_LOCATION);
                long maxFileSize = properties.getLongValue(WebConfigKey.WEB_MULITPART_MAX_FILE_SIZE,
                        WebConfigDefault.WEB_MULITPART_MAX_FILE_SIZE);
                long maxRequestSize = properties.getLongValue(WebConfigKey.WEB_MULITPART_MAX_REQUEST_SIZE,
                        WebConfigDefault.WEB_MULITPART_MAX_REQUEST_SIZE);
                int fileSizeThreshold = properties.getIntValue(WebConfigKey.WEB_MULITPART_FILE_SIZE_THRESHOLD,
                        WebConfigDefault.WEB_MULITPART_FILE_SIZE_THRESHOLD);

                MultiPartConfig result = new MultiPartConfig(location, maxFileSize, maxRequestSize, fileSizeThreshold);

                return result;
            }

        });

        return (MultiPartConfig) cachedConfig;
    }

    public static ProjectMode getProjectMode() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.PROJECT_MODE, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {
                return properties.getEnumValue(WebConfigKey.PROJECT_MODE, WebConfigDefault.PROJECT_MODE, true);
            }

        });

        return (ProjectMode) cachedConfig;
    }

    public static String getProjectVersion() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.PROJECT_VERSION, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {
                try {
                    return properties.get(WebConfigKey.PROJECT_VERSION);
                } catch (PropertyNotFoundException e) {
                    throw new RuntimeException("Couldn't load " + WebConfigKey.PROJECT_VERSION);
                }
            }

        });

        return (String) cachedConfig;
    }

    public static String getProjectBuildtime() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.PROJECT_BUILDTIME, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {
                try {
                    return properties.get(WebConfigKey.PROJECT_BUILDTIME);
                } catch (PropertyNotFoundException e) {
                    throw new RuntimeException("Couldn't load " + WebConfigKey.PROJECT_BUILDTIME);
                }
            }

        });

        return (String) cachedConfig;
    }

    public static String getCopyrightHolder() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.PROJECT_COPYRIGHT_HOLDER, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {
                return properties.get(WebConfigKey.PROJECT_COPYRIGHT_HOLDER, WebConfigDefault.PROJECT_COPYRIGHT_HOLDER);
            }

        });

        return (String) cachedConfig;
    }

    public static String getSitename() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_SITENAME, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {
                return properties.get(WebConfigKey.WEB_SITENAME, WebConfigDefault.WEB_SITENAME);
            }

        });

        return (String) cachedConfig;
    }

    public static String getAssetsPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ASSETS_PATH, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {
                return properties.get(WebConfigKey.WEB_ASSETS_PATH, WebConfigDefault.WEB_ASSETS_PATH);
            }

        });

        return (String) cachedConfig;
    }

    /**
     * 检查当前配置是不是调试模式<br>
     * <b>注意:</b>
     * 根据配置不同,调试模式可能包含多个{@link ProjectMode},并不是{@link ProjectMode#DEVELOP}
     * 
     * @return
     */
    public static boolean isDebugMode() {
        // TODO: make logdetailsmode configable
        return getProjectMode().equals(ProjectMode.DEVELOP) || getProjectMode().equals(ProjectMode.DEBUG);
    }

    public static String getDefaultTheme() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_DEFAULT_THEME, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {
                return properties.get(WebConfigKey.WEB_DEFAULT_THEME, WebConfigDefault.WEB_DEFAULT_THEME);
            }

        });

        return (String) cachedConfig;
    }

    public static String getAdminDashboardBrandIcon() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_ICON,
                new DataGetter<String, Object>() {

                    @Override
                    public Object getData(String key) {
                        return properties.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_ICON,
                                WebConfigDefault.WEB_ADMIN_DASHBOARD_BRAND_ICON);
                    }

                });

        return (String) cachedConfig;
    }

    public static String getAdminDashboardBrandText() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_TEXT,
                new DataGetter<String, Object>() {

                    @Override
                    public Object getData(String key) {
                        return properties.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_TEXT,
                                WebConfigDefault.WEB_ADMIN_DASHBOARD_BRAND_TEXT);
                    }

                });

        return (String) cachedConfig;
    }

    public static String getRootContextConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_ROOT_CONTEXT_CONFIG_CLASS, key -> {
            return properties.get(key, WebConfigDefault.CORE_ROOT_CONTEXT_CONFIG_CLASS);
        });

        return (String) cachedConfig;
    }

    public static String getWebConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_WEB_CONFIG_CLASS, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {
                return properties.get(WebConfigKey.CORE_WEB_CONFIG_CLASS, WebConfigDefault.CORE_WEB_CONFIG_CLASS);
            }

        });

        return (String) cachedConfig;
    }

    public static String getAjaxConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_AJAX_CONFIG_CLASS, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {
                return properties.get(WebConfigKey.CORE_AJAX_CONFIG_CLASS, WebConfigDefault.CORE_AJAX_CONFIG_CLASS);
            }

        });

        return (String) cachedConfig;
    }

    public static String getAdminWebConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_ADMIN_WEB_CONFIG_CLASS,
                new DataGetter<String, Object>() {

                    @Override
                    public Object getData(String key) {
                        return properties.get(WebConfigKey.CORE_ADMIN_WEB_CONFIG_CLASS,
                                WebConfigDefault.CORE_ADMIN_WEB_CONFIG_CLASS);
                    }

                });

        return (String) cachedConfig;
    }

    public static String getAdminAjaxConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_ADMIN_AJAX_CONFIG_CLASS, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {
                return properties.get(WebConfigKey.CORE_ADMIN_AJAX_CONFIG_CLASS, WebConfigDefault.CORE_ADMIN_AJAX_CONFIG_CLASS);
            }

        });

        return (String) cachedConfig;
    }

    public static String getApiConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_API_CONFIG_CLASS, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {
                return properties.get(WebConfigKey.CORE_API_CONFIG_CLASS, WebConfigDefault.CORE_API_CONFIG_CLASS);
            }

        });

        return (String) cachedConfig;
    }

    /**
     * 获得站点默认语言
     * @return 站点默认语言, 例如 zh_CN, en_US
     */
    public static String getDefaultLanguage() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_DEFAULT_LANGUAGE, key -> {
            try {
                return properties.get(key);
            } catch (PropertyNotFoundException e) {
                return null;
            }
        });

        if(cachedConfig == null) {
            return null;            
        }
        
        return (String) cachedConfig;
    }

}
