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
        private static final String WEB_UPLOAD_PATH = "web.uploadPath";
        private static final String WEB_JSP_PATH = "web.jspPath";
        private static final String WEB_ADMIN_JSP_PATH = "web.admin.JspPath";
        private static final String WEB_ADMIN_ROOT_PATH = "web.admin.rootPath";
        private static final String WEB_ADMIN_DASHBOARD_BRAND_ICON = "web.admin.dashboardBrandIcon";
        private static final String WEB_ADMIN_DASHBOARD_BRAND_TEXT = "web.admin.dashboardBrandText";

        private static final String WEB_API_ENABLED = "web.api.enabled";
        private static final String WEB_API_ROOT_PATH = "web.api.rootPath";
        private static final String WEB_ASSETS_PATH = "web.asstesPath";

        private static final String WEB_MULITPART = "web.multipart";
        private static final String WEB_MULITPART_LOCATION = "web.multiPart.location";
        private static final String WEB_MULITPART_MAX_FILE_SIZE = "web.multiPart.maxFileSize";
        private static final String WEB_MULITPART_MAX_REQUEST_SIZE = "web.multiPart.maxRequestSize";
        private static final String WEB_MULITPART_FILE_SIZE_THRESHOLD = "web.multiPart.fileSizeThreshold";

        // [security]
        private static final String SECURITY_WEB_AUTHENTICATION_TYPE = "security.web.authenticationType";
        private static final String SECURITY_API_AUTHENTICATION_TYPE = "security.api.authenticationType";
        private static final String SECURITY_OAUTH_SERVER_TYPE = "security.oauth.severType";

        private static final String SECURITY_AUTHENTICATION_ENABLE_EMAIL_SIGNIN = "security.authentication.enableEmailSignin";
        private static final String SECURITY_AUTHENTICATION_ENABLE_MOBILE_SIGNIN = "security.authentication.enableMobileSignin";
        // private static final String SECURITY_AUTHENTICATION_ENABLE_USER_CAHCE = "security.authentication.enableUserCache";
        // private static final String SECURITY_AUTHENTICATION_USER_CAHCE_LIFE = "security.authentication.userCacheLife";
        private static final String SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE = "security.authentication.userContext.cacheLife";

        private static final String SECURITY_SIGNUP_USERNAME_FORMAT = "security.signup.username.format";
        private static final String SECURITY_SIGNUP_EMAIL_FORMAT = "security.signup.email.format";
        private static final String SECURITY_SIGNUP_PASSWORD_FORMAT = "security.signup.password.format";
        private static final String SECURITY_SIGNUP_PASSWORD_MIN_LENGTH = "security.signup.password.minLength";
        private static final String SECURITY_SIGNUP_AUTO_SIGNIN = "security.signup.autoSignin";
    }

    private static class WebConfigDefault {
        private static final String PROJECT_COPYRIGHT_HOLDER = "Copyright Holder";
        private static final ProjectMode PROJECT_MODE = ProjectMode.DEBUG;

        private static final String CORE_ROOT_CONTEXT_CONFIG_CLASS = "net.eulerframework.config.RootContextConfig";
        private static final String CORE_WEB_CONFIG_CLASS = "net.eulerframework.config.WebServletContextConfig";
        private static final String CORE_AJAX_CONFIG_CLASS = "net.eulerframework.config.AjaxServletContextConfig";
        private static final String CORE_ADMIN_WEB_CONFIG_CLASS = "net.eulerframework.config.AdminWebServletContextConfig";
        private static final String CORE_ADMIN_AJAX_CONFIG_CLASS = "net.eulerframework.config.AdminAjaxServletContextConfig";
        private static final String CORE_API_CONFIG_CLASS = "net.eulerframework.config.ApiServletContextConfig";
        private static final int CORE_CACHE_I18N_REFRESH_FREQ = 86_400;
        private static final long CORE_CAHCE_RAMCACHE_POOL_CLEAN_FREQ = 60_000L;

        private static final String WEB_SITENAME = "DEMO";
        private static final String WEB_DEFAULT_THEME = "default";
        private static final String WEB_UPLOAD_PATH_UNIX = "file:///var/lib/euler-framework/archive/files";
        private static final String WEB_UPLOAD_PATH_WIN = "file://C:\\euler-framework-data\\archive\\files";
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

        private static final WebAuthenticationType SECURITY_WEB_AUTHENTICATION_TYPE = WebAuthenticationType.LOCAL;
        private static final ApiAuthenticationType SECURITY_API_AUTHENTICATION_TYPE = ApiAuthenticationType.NONE;
        private static final OAuthServerType SECURITY_OAUTH_SERVER_TYPE = OAuthServerType.NEITHER;
        private static final boolean SECURITY_AUTHENTICATION_ENABLE_EMAIL_SIGNIN = false;
        private static final boolean SECURITY_AUTHENTICATION_ENABLE_MOBILE_SIGNIN = false;
        // private static final boolean SECURITY_AUTHENTICATION_ENABLE_USER_CAHCE = false;
        // private static final long SECURITY_AUTHENTICATION_USER_CAHCE_LIFE = 0;
        private static final long SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE = 600_000L;

        private static final String SECURITY_SIGNUP_USERNAME_FORMAT = "^[A-Za-z][A-Za-z0-9_\\-\\.]+[A-Za-z0-9]$"; // 至少三位，以字母开头，中间可含有字符数字_-.,以字母或数字结尾
        private static final String SECURITY_SIGNUP_EMAIL_FORMAT = "^[A-Za-z0-9_\\-\\.]+@[a-zA-Z0-9_\\-]+(\\.[a-zA-Z0-9_\\-]+)+$"; // 可含有-_.的email
        private static final String SECURITY_SIGNUP_PASSWORD_FORMAT = "^[\\u0021-\\u007e]+$"; // ASCII可显示非空白字符
        private static final int SECURITY_SIGNUP_PASSWORD_MIN_LENGTH = 6;
        private static final boolean SECURITY_SIGNUP_AUTO_SIGNIN = true;

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

    public static WebAuthenticationType getWebAuthenticationType() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_WEB_AUTHENTICATION_TYPE, key -> {
            return properties.getEnumValue(key,
                        WebConfigDefault.SECURITY_WEB_AUTHENTICATION_TYPE, true);
        });

        return (WebAuthenticationType) cachedConfig;
    }

    public static ApiAuthenticationType getApiAuthenticationType() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_API_AUTHENTICATION_TYPE, key -> {
            return properties.getEnumValue(WebConfigKey.SECURITY_API_AUTHENTICATION_TYPE,
                                WebConfigDefault.SECURITY_API_AUTHENTICATION_TYPE, true);
        });
        return (ApiAuthenticationType) cachedConfig;
    }

    public static OAuthServerType getOAuthSeverType() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_OAUTH_SERVER_TYPE, key -> {
            return properties.getEnumValue(key,
                                WebConfigDefault.SECURITY_OAUTH_SERVER_TYPE, true);
        });
        return (OAuthServerType) cachedConfig;
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

            result = CommonUtils.convertDirToUnixFormat(result);

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

                result = CommonUtils.convertDirToUnixFormat(result);

                if (!result.startsWith("/"))
                    result = "/" + result;

                return result;
            }
        });

        return (String) cachedConfig;
    }

    public static String getUploadPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_UPLOAD_PATH, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {

                String result;
                try {
                    result = CommonUtils.convertDirToUnixFormat(properties.get(WebConfigKey.WEB_UPLOAD_PATH));
                } catch (PropertyNotFoundException e) {
                    if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
                        LOGGER.info("OS is windows");
                        result = WebConfigDefault.WEB_UPLOAD_PATH_WIN;
                    } else {
                        LOGGER.info("OS isn't windows");
                        result = WebConfigDefault.WEB_UPLOAD_PATH_UNIX;
                    }
                    LOGGER.warn("Couldn't load " + WebConfigKey.WEB_UPLOAD_PATH + " , use " + result + " for default.");
                }

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

    public static String getJspPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_JSP_PATH, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {

                String result = CommonUtils.convertDirToUnixFormat(
                        properties.get(WebConfigKey.WEB_JSP_PATH, WebConfigDefault.WEB_JSP_PATH));
                // 统一添加/结尾，这样在controller中就可以不加/前缀
                result = result + "/";

                return result;
            }

        });

        return (String) cachedConfig;
    }

    public static String getAdminJspPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ADMIN_JSP_PATH, new DataGetter<String, Object>() {

            @Override
            public Object getData(String key) {

                String result = CommonUtils.convertDirToUnixFormat(
                        properties.get(WebConfigKey.WEB_ADMIN_JSP_PATH, WebConfigDefault.WEB_ADMIN_JSP_PATH));
                // 统一添加/结尾，这样在controller中就可以不加/前缀
                result = result + "/";

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

    public static int getMinPasswordLength() {

        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_SIGNUP_PASSWORD_MIN_LENGTH,
                new DataGetter<String, Object>() {

                    @Override
                    public Object getData(String key) {

                        int result = properties.getIntValue(WebConfigKey.SECURITY_SIGNUP_PASSWORD_MIN_LENGTH,
                                WebConfigDefault.SECURITY_SIGNUP_PASSWORD_MIN_LENGTH);

                        if (result > getMaxPasswordLength()) {
                            result = getMaxPasswordLength();
                            LOGGER.warn("Password length must less than " + result + ", use " + result + " as "
                                    + WebConfigKey.SECURITY_SIGNUP_PASSWORD_MIN_LENGTH);
                        }

                        return result;
                    }

                });

        return (int) cachedConfig;

    }

    public static int getMaxPasswordLength() {
        return 20;
    }

    public static String getUsernameFormat() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_SIGNUP_USERNAME_FORMAT,
                new DataGetter<String, Object>() {

                    @Override
                    public Object getData(String key) {
                        return properties.get(WebConfigKey.SECURITY_SIGNUP_USERNAME_FORMAT,
                                WebConfigDefault.SECURITY_SIGNUP_USERNAME_FORMAT);
                    }

                });

        return (String) cachedConfig;
    }

    public static String getEmailFormat() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_SIGNUP_EMAIL_FORMAT,
                new DataGetter<String, Object>() {

                    @Override
                    public Object getData(String key) {
                        return properties.get(WebConfigKey.SECURITY_SIGNUP_EMAIL_FORMAT,
                                WebConfigDefault.SECURITY_SIGNUP_EMAIL_FORMAT);
                    }

                });

        return (String) cachedConfig;
    }

    public static String getPasswordFormat() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_SIGNUP_PASSWORD_FORMAT,
                new DataGetter<String, Object>() {

                    @Override
                    public Object getData(String key) {
                        return properties.get(WebConfigKey.SECURITY_SIGNUP_PASSWORD_FORMAT,
                                WebConfigDefault.SECURITY_SIGNUP_PASSWORD_FORMAT);
                    }

                });

        return (String) cachedConfig;
    }

    public static boolean isEnableEmailSignin() {

        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_AUTHENTICATION_ENABLE_EMAIL_SIGNIN,
                new DataGetter<String, Object>() {

                    @Override
                    public Object getData(String key) {
                        return properties.getBooleanValue(WebConfigKey.SECURITY_AUTHENTICATION_ENABLE_EMAIL_SIGNIN,
                                WebConfigDefault.SECURITY_AUTHENTICATION_ENABLE_EMAIL_SIGNIN);
                    }

                });

        return (boolean) cachedConfig;
    }

    public static boolean isEnableMobileSignin() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_AUTHENTICATION_ENABLE_MOBILE_SIGNIN,
                new DataGetter<String, Object>() {

                    @Override
                    public Object getData(String key) {
                        return properties.getBooleanValue(WebConfigKey.SECURITY_AUTHENTICATION_ENABLE_MOBILE_SIGNIN,
                                WebConfigDefault.SECURITY_AUTHENTICATION_ENABLE_MOBILE_SIGNIN);
                    }

                });

        return (boolean) cachedConfig;
    }

    public static long getUserContextCacheLife() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE,
                new DataGetter<String, Object>() {

                    @Override
                    public Object getData(String key) {
                        return properties.getLongValue(WebConfigKey.SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE,
                                WebConfigDefault.SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE);
                    }

                });

        return (long) cachedConfig;
    }

    public static boolean isEnableAutoSigninAfterSignup() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_SIGNUP_AUTO_SIGNIN,
                new DataGetter<String, Object>() {

                    @Override
                    public Object getData(String key) {
                        return properties.getBooleanValue(WebConfigKey.SECURITY_SIGNUP_AUTO_SIGNIN,
                                WebConfigDefault.SECURITY_SIGNUP_AUTO_SIGNIN);
                    }

                });

        return (boolean) cachedConfig;
    }

    public static boolean isEnableAutoAuthorizeAfterSignup() {
        // TODO Auto-generated method stub
        return false;
    }

    public static String[] getAutoAuthorizeGroupId() {
        // TODO Auto-generated method stub
        return new String[] { "8a775fcf-6f3e-4b57-8a1a-a9bd96a4bf49" };
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
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_ROOT_CONTEXT_CONFIG_CLASS,
                new DataGetter<String, Object>() {

                    @Override
                    public Object getData(String key) {
                        return properties.get(WebConfigKey.CORE_ROOT_CONTEXT_CONFIG_CLASS,
                                WebConfigDefault.CORE_ROOT_CONTEXT_CONFIG_CLASS);
                    }

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

}
