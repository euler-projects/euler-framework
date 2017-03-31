package net.eulerframework.web.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ContextLoader;

import net.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import net.eulerframework.cache.inMemoryCache.ObjectCachePool;
import net.eulerframework.common.util.CommonUtils;
import net.eulerframework.common.util.StringUtils;
import net.eulerframework.common.util.property.PropertyNotFoundException;
import net.eulerframework.common.util.property.PropertyReader;

@Configuration
public abstract class WebConfig {

    private static final DefaultObjectCache<String, Object> CONFIG_CAHCE = ObjectCachePool
            .generateDefaultObjectCache(Long.MAX_VALUE);
    
    private static final PropertyReader properties = new PropertyReader("/config.properties");

    private static class WebConfigKey {
        //[project]
        private static final String PROJECT_VERSION = "project.verison";
        private static final String PROJECT_MODE = "project.mode";
        private static final String PROJECT_BUILDTIME = "project.buildtime";
        private static final String PROJECT_COPYRIGHT_HOLDER = "project.copyrightHolder";
        
        //[core]
        private static final String CORE_ROOT_CONTEXT_CONFIG_CLASS = "core.rootContextConfigClass";
        private static final String CORE_WEB_CONFIG_CLASS = "core.webConfigClass";
        private static final String CORE_ADMIN_WEB_CONFIG_CLASS = "core.adminWebConfigClass";
        private static final String CORE_API_CONFIG_CLASS = "core.apiConfigClass";
        private static final String CORE_CACHE_I18N_REFRESH_FREQ = "core.cache.i18n.refreshFreq";
        private static final String CORE_CAHCE_RAMCACHE_POOL_CLEAN_FREQ = "core.cache.ramCachePool.cleanFreq";

        //[web]
        private static final String WEB_SITENAME = "web.sitename";
        private static final String WEB_DEFAULT_THEME = "web.defaultTheme";
        private static final String WEB_UPLOAD_PATH = "web.uploadPath";
        private static final String WEB_JSP_PATH = "web.jspPath";
        private static final String WEB_ADMIN_JSP_PATH = "web.admin.JspPath";
        private static final String WEB_ADMIN_ROOT_PATH = "web.admin.rootPath";
        private static final String WEB_ADMIN_DASHBOARD_BRAND_ICON = "web.admin.dashboardBrandIcon";
        private static final String WEB_ADMIN_DASHBOARD_BRAND_TEXT = "web.admin.dashboardBrandText";
        private static final String WEB_API_ROOT_PATH = "web.api.rootPath";
        private static final String WEB_ASSETS_PATH = "web.asstesPath";
        
        private static final String WEB_MULITPART = "web.multipart";
        private static final String WEB_MULITPART_LOCATION = "web.multiPart.location";
        private static final String WEB_MULITPART_MAX_FILE_SIZE = "web.multiPart.maxFileSize";
        private static final String WEB_MULITPART_MAX_REQUEST_SIZE = "web.multiPart.maxRequestSize";
        private static final String WEB_MULITPART_FILE_SIZE_THRESHOLD = "web.multiPart.fileSizeThreshold";

        //[security]
        private static final String SECURITY_WEB_AUTHENTICATION_TYPE = "security.web.authenticationType";
        private static final String SECURITY_API_AUTHENTICATION_TYPE = "security.api.authenticationType";
        private static final String SECURITY_OAUTH_SERVER_TYPE = "security.oauth.severType";
        
        private static final String SECURITY_AUTHENTICATION_ENABLE_EMAIL_SIGNIN = "security.authentication.enableEmailSignin";
        private static final String SECURITY_AUTHENTICATION_ENABLE_MOBILE_SIGNIN = "security.authentication.enableMobileSignin";
        private static final String SECURITY_AUTHENTICATION_ENABLE_USER_CAHCE = "security.authentication.enableUserCache";
        private static final String SECURITY_AUTHENTICATION_USER_CAHCE_LIFE = "security.authentication.userCacheLife";
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
        

        private static final String CORE_ROOT_CONTEXT_CONFIG_CLASS = "net.eulerframework.config.RootContextConfiguration";
        private static final String CORE_WEB_CONFIG_CLASS = "net.eulerframework.config.SpringWebDispatcherServletContextConfiguration";
        private static final String CORE_ADMIN_WEB_CONFIG_CLASS = "net.eulerframework.config.SpringAdminWebDispatcherServletContextConfiguration";
        private static final String CORE_API_CONFIG_CLASS = "net.eulerframework.config.SpringApiDispatcherServletContextConfiguration";
        private static final int CORE_CACHE_I18N_REFRESH_FREQ = 86_400;
        private static final long CORE_CAHCE_RAMCACHE_POOL_CLEAN_FREQ = 60_000L;

        private static final String WEB_SITENAME = "DEMO";
        private static final String WEB_DEFAULT_THEME = "default";
        private static final String WEB_UPLOAD_PATH_UNIX = "file:///var/lib/euler-framework/archive/files";
        private static final String WEB_UPLOAD_PATH_WIN = "file://C:\\euler-framework-data\\archive\\files";
        private static final String WEB_JSP_PATH = "/WEB-INF/jsp/themes";
        private static final String WEB_ADMIN_JSP_PATH = "/WEB-INF/jsp/admin/themes";
        //private static final String WEB_ADMIN_ROOT_PATH = "/admin";
        private static final String WEB_ADMIN_DASHBOARD_BRAND_ICON = "/assets/system/admin-dashboard-brand.png";
        private static final String WEB_ADMIN_DASHBOARD_BRAND_TEXT = "Manage Dashboard";
        //private static final String WEB_API_ROOT_PATH = "/api";
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
        private static final boolean SECURITY_AUTHENTICATION_ENABLE_USER_CAHCE = false;
        private static final long SECURITY_AUTHENTICATION_USER_CAHCE_LIFE = 0;
        private static final long SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE = 600_000L;

        private static final String SECURITY_SIGNUP_USERNAME_FORMAT = "^[A-Za-z][A-Za-z0-9_\\-\\.]+[A-Za-z0-9]$"; //至少三位，以字母开头，中间可含有字符数字_-.,以字母或数字结尾
        private static final String SECURITY_SIGNUP_EMAIL_FORMAT = "^[A-Za-z0-9_\\-\\.]+@[a-zA-Z0-9_\\-]+(\\.[a-zA-Z0-9_\\-]+)+$"; //可含有-_.的email
        private static final String SECURITY_SIGNUP_PASSWORD_FORMAT = "^[\\u0021-\\u007e]+$"; //ASCII可显示非空白字符
        private static final int SECURITY_SIGNUP_PASSWORD_MIN_LENGTH = 6;
        private static final boolean SECURITY_SIGNUP_AUTO_SIGNIN = true;

    }

    protected static final Logger log = LogManager.getLogger();

    public static boolean clearWebConfigCache() {
        properties.refresh();
        return CONFIG_CAHCE.clear();
    }

    public static int getI18nRefreshFreq() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_CACHE_I18N_REFRESH_FREQ);
        if (cachedConfig != null) {
            return (int) cachedConfig;
        }

        int result = properties.getIntValue(WebConfigKey.CORE_CACHE_I18N_REFRESH_FREQ,
                WebConfigDefault.CORE_CACHE_I18N_REFRESH_FREQ);

        CONFIG_CAHCE.put(WebConfigKey.CORE_CACHE_I18N_REFRESH_FREQ, result);
        return result;
    }

    public static WebAuthenticationType getWebAuthenticationType() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_WEB_AUTHENTICATION_TYPE);
        if (cachedConfig != null) {
            return (WebAuthenticationType) cachedConfig;
        }

        WebAuthenticationType result = properties.getEnumValue(WebConfigKey.SECURITY_WEB_AUTHENTICATION_TYPE,
                WebConfigDefault.SECURITY_WEB_AUTHENTICATION_TYPE,
                true);

        CONFIG_CAHCE.put(WebConfigKey.SECURITY_WEB_AUTHENTICATION_TYPE, result);
        return result;
    }

    public static ApiAuthenticationType getApiAuthenticationType() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_API_AUTHENTICATION_TYPE);
        if (cachedConfig != null) {
            return (ApiAuthenticationType) cachedConfig;
        }

        ApiAuthenticationType result = properties.getEnumValue(WebConfigKey.SECURITY_API_AUTHENTICATION_TYPE,
                WebConfigDefault.SECURITY_API_AUTHENTICATION_TYPE,
                true);

        CONFIG_CAHCE.put(WebConfigKey.SECURITY_API_AUTHENTICATION_TYPE, result);
        return result;
    }

    public static OAuthServerType getOAuthSeverType() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_OAUTH_SERVER_TYPE);
        if (cachedConfig != null) {
            return (OAuthServerType) cachedConfig;
        }

        OAuthServerType result = properties.getEnumValue(WebConfigKey.SECURITY_OAUTH_SERVER_TYPE,
                WebConfigDefault.SECURITY_OAUTH_SERVER_TYPE,
                true);

        CONFIG_CAHCE.put(WebConfigKey.SECURITY_OAUTH_SERVER_TYPE, result);
        return result;
    }

    @Bean
    public static String getApiRootPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_API_ROOT_PATH);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result;
        try {
            result = properties.get(WebConfigKey.WEB_API_ROOT_PATH);
        } catch (PropertyNotFoundException e) {
            throw new RuntimeException(WebConfigKey.WEB_API_ROOT_PATH + " can not be empty");
        }

        if (!StringUtils.hasText(result))
            throw new RuntimeException(WebConfigKey.WEB_API_ROOT_PATH + "can not be empty");

        while (result.endsWith("*")) {
            result = result.substring(0, result.length() - 1);
        }

        result = CommonUtils.convertDirToUnixFormat(result);

        if (!result.startsWith("/"))
            result = "/" + result;

        CONFIG_CAHCE.put(WebConfigKey.WEB_API_ROOT_PATH, result);
        return result;
    }

    public static String getAdminRootPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ADMIN_ROOT_PATH);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result;
        try {
            result = properties.get(WebConfigKey.WEB_ADMIN_ROOT_PATH);
        } catch (PropertyNotFoundException e) {
            throw new RuntimeException(WebConfigKey.WEB_ADMIN_ROOT_PATH + " can not be empty");
        }

        if (!StringUtils.hasText(result))
            throw new RuntimeException(WebConfigKey.WEB_ADMIN_ROOT_PATH + " can not be empty");

        while (result.endsWith("*")) {
            result = result.substring(0, result.length() - 1);
        }

        result = CommonUtils.convertDirToUnixFormat(result);

        if (!result.startsWith("/"))
            result = "/" + result;

        CONFIG_CAHCE.put(WebConfigKey.WEB_ADMIN_ROOT_PATH, result);
        return result;
    }

    public static String getUploadPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_UPLOAD_PATH);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result;
        try {
            result = CommonUtils.convertDirToUnixFormat(properties.get(WebConfigKey.WEB_UPLOAD_PATH));
        } catch (PropertyNotFoundException e) {
            if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
                log.info("OS is windows");
                result = WebConfigDefault.WEB_UPLOAD_PATH_WIN;
            } else {
                log.info("OS isn't windows");
                result = WebConfigDefault.WEB_UPLOAD_PATH_UNIX;
            }
            log.warn("Couldn't load " + WebConfigKey.WEB_UPLOAD_PATH + " , use " + result + " for default.");
        }

        if (!result.startsWith("/") && !result.startsWith("file://")) {
            result = ContextLoader.getCurrentWebApplicationContext().getServletContext().getRealPath(result);
        } else {
            if (result.startsWith("file://")) {
                result = result.substring("file://".length());
            }
        }

        CONFIG_CAHCE.put(WebConfigKey.WEB_UPLOAD_PATH, result);
        return result;

    }

    public static String getJspPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_JSP_PATH);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = CommonUtils
                .convertDirToUnixFormat(properties.get(WebConfigKey.WEB_JSP_PATH, WebConfigDefault.WEB_JSP_PATH));
        // 统一添加/结尾，这样在controller中就可以不加/前缀
        result = result + "/";

        CONFIG_CAHCE.put(WebConfigKey.WEB_JSP_PATH, result);
        return result;
    }

    public static String getAdminJspPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ADMIN_JSP_PATH);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = CommonUtils.convertDirToUnixFormat(
                properties.get(WebConfigKey.WEB_ADMIN_JSP_PATH, WebConfigDefault.WEB_ADMIN_JSP_PATH));
        // 统一添加/结尾，这样在controller中就可以不加/前缀
        result = result + "/";

        CONFIG_CAHCE.put(WebConfigKey.WEB_ADMIN_JSP_PATH, result);
        return result;
    }

    public static long getRamCacheCleanFreq() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_CAHCE_RAMCACHE_POOL_CLEAN_FREQ);
        if (cachedConfig != null) {
            return (long) cachedConfig;
        }

        long result = properties.getLongValue(WebConfigKey.CORE_CAHCE_RAMCACHE_POOL_CLEAN_FREQ,
                WebConfigDefault.CORE_CAHCE_RAMCACHE_POOL_CLEAN_FREQ);

        CONFIG_CAHCE.put(WebConfigKey.CORE_CAHCE_RAMCACHE_POOL_CLEAN_FREQ, result);
        return result;
    }

    public static MultiPartConfig getMultiPartConfig() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_MULITPART);
        if (cachedConfig != null) {
            return (MultiPartConfig) cachedConfig;
        }

        String location = properties.get(WebConfigKey.WEB_MULITPART_LOCATION,
                WebConfigDefault.WEB_MULITPART_LOCATION);
        long maxFileSize = properties.getLongValue(WebConfigKey.WEB_MULITPART_MAX_FILE_SIZE,
                WebConfigDefault.WEB_MULITPART_MAX_FILE_SIZE);
        long maxRequestSize = properties.getLongValue(WebConfigKey.WEB_MULITPART_MAX_REQUEST_SIZE,
                WebConfigDefault.WEB_MULITPART_MAX_REQUEST_SIZE);
        int fileSizeThreshold = properties.getIntValue(WebConfigKey.WEB_MULITPART_FILE_SIZE_THRESHOLD,
                WebConfigDefault.WEB_MULITPART_FILE_SIZE_THRESHOLD);

        MultiPartConfig result = new MultiPartConfig(location, maxFileSize, maxRequestSize, fileSizeThreshold);

        CONFIG_CAHCE.put(WebConfigKey.WEB_MULITPART, result);
        return result;
    }

    public static int getMinPasswordLength() {

        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_SIGNUP_PASSWORD_MIN_LENGTH);
        if (cachedConfig != null) {
            return (int) cachedConfig;
        }

        int result = properties.getIntValue(WebConfigKey.SECURITY_SIGNUP_PASSWORD_MIN_LENGTH,
                WebConfigDefault.SECURITY_SIGNUP_PASSWORD_MIN_LENGTH);
        
        if(result > getMaxPasswordLength()) {
            result = getMaxPasswordLength();
            log.warn("Password length must less than " + result + ", use " + result + " as " + WebConfigKey.SECURITY_SIGNUP_PASSWORD_MIN_LENGTH);
        }

        CONFIG_CAHCE.put(WebConfigKey.SECURITY_SIGNUP_PASSWORD_MIN_LENGTH, result);
        return result;
    }
    
    public static int getMaxPasswordLength() {
        return 20;
    }

    public static String getUsernameFormat() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_SIGNUP_USERNAME_FORMAT);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.SECURITY_SIGNUP_USERNAME_FORMAT,
                WebConfigDefault.SECURITY_SIGNUP_USERNAME_FORMAT);

        CONFIG_CAHCE.put(WebConfigKey.SECURITY_SIGNUP_USERNAME_FORMAT, result);
        return result;
    }

    public static String getEmailFormat() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_SIGNUP_EMAIL_FORMAT);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.SECURITY_SIGNUP_EMAIL_FORMAT,
                WebConfigDefault.SECURITY_SIGNUP_EMAIL_FORMAT);

        CONFIG_CAHCE.put(WebConfigKey.SECURITY_SIGNUP_EMAIL_FORMAT, result);
        return result;
    }

    public static String getPasswordFormat() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_SIGNUP_PASSWORD_FORMAT);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.SECURITY_SIGNUP_PASSWORD_FORMAT,
                WebConfigDefault.SECURITY_SIGNUP_PASSWORD_FORMAT);

        CONFIG_CAHCE.put(WebConfigKey.SECURITY_SIGNUP_PASSWORD_FORMAT, result);
        return result;
    }

    public static boolean isEnableEmailSignin() {

        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_AUTHENTICATION_ENABLE_EMAIL_SIGNIN);
        if (cachedConfig != null) {
            return (boolean) cachedConfig;
        }

        boolean result = properties.getBooleanValue(WebConfigKey.SECURITY_AUTHENTICATION_ENABLE_EMAIL_SIGNIN,
                WebConfigDefault.SECURITY_AUTHENTICATION_ENABLE_EMAIL_SIGNIN);

        CONFIG_CAHCE.put(WebConfigKey.SECURITY_AUTHENTICATION_ENABLE_EMAIL_SIGNIN, result);
        return result;
    }

    public static boolean isEnableMobileSignin() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_AUTHENTICATION_ENABLE_MOBILE_SIGNIN);
        if (cachedConfig != null) {
            return (boolean) cachedConfig;
        }

        boolean result = properties.getBooleanValue(WebConfigKey.SECURITY_AUTHENTICATION_ENABLE_MOBILE_SIGNIN,
                WebConfigDefault.SECURITY_AUTHENTICATION_ENABLE_MOBILE_SIGNIN);

        CONFIG_CAHCE.put(WebConfigKey.SECURITY_AUTHENTICATION_ENABLE_MOBILE_SIGNIN, result);
        return result;
    }

    public static boolean isEnableUserCache() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_AUTHENTICATION_ENABLE_USER_CAHCE);
        if (cachedConfig != null) {
            return (boolean) cachedConfig;
        }

        boolean result = properties.getBooleanValue(WebConfigKey.SECURITY_AUTHENTICATION_ENABLE_USER_CAHCE,
                WebConfigDefault.SECURITY_AUTHENTICATION_ENABLE_USER_CAHCE);

        CONFIG_CAHCE.put(WebConfigKey.SECURITY_AUTHENTICATION_ENABLE_USER_CAHCE, result);
        return result;
    }

    public static long getUserCacheLife() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_AUTHENTICATION_USER_CAHCE_LIFE);
        if (cachedConfig != null) {
            return (long) cachedConfig;
        }

        long result = properties.getLongValue(WebConfigKey.SECURITY_AUTHENTICATION_USER_CAHCE_LIFE,
                WebConfigDefault.SECURITY_AUTHENTICATION_USER_CAHCE_LIFE);

        CONFIG_CAHCE.put(WebConfigKey.SECURITY_AUTHENTICATION_USER_CAHCE_LIFE, result);
        return result;
    }

    public static long getUserContextCacheLife() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE);
        if (cachedConfig != null) {
            return (long) cachedConfig;
        }
    
        long result = properties.getLongValue(WebConfigKey.SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE,
                WebConfigDefault.SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE);
    
        CONFIG_CAHCE.put(WebConfigKey.SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE, result);
        return result;
    }

    public static boolean isEnableAutoSigninAfterSignup() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_SIGNUP_AUTO_SIGNIN);
        if (cachedConfig != null) {
            return (boolean) cachedConfig;
        }
    
        boolean result = properties.getBooleanValue(WebConfigKey.SECURITY_SIGNUP_AUTO_SIGNIN,
                WebConfigDefault.SECURITY_SIGNUP_AUTO_SIGNIN);
    
        CONFIG_CAHCE.put(WebConfigKey.SECURITY_SIGNUP_AUTO_SIGNIN, result);
        return result;
    }

    public static boolean isEnableAutoAuthorizeAfterSignup() {
        // TODO Auto-generated method stub
        return false;
    }

    public static String[] getAutoAuthorizeGroupId() {
        // TODO Auto-generated method stub
        return new String[]{"8a775fcf-6f3e-4b57-8a1a-a9bd96a4bf49"};
    }

    public static ProjectMode getProjectMode() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.PROJECT_MODE);
        if (cachedConfig != null) {
            return (ProjectMode) cachedConfig;
        }

        ProjectMode result = properties.getEnumValue(WebConfigKey.PROJECT_MODE,
                WebConfigDefault.PROJECT_MODE,
                true);

        CONFIG_CAHCE.put(WebConfigKey.PROJECT_MODE, result);
        return result;
    }

    public static String getProjectVersion() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.PROJECT_VERSION);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result;
        try {
            result = properties.get(WebConfigKey.PROJECT_VERSION);
        } catch (PropertyNotFoundException e) {
            throw new RuntimeException("Couldn't load " + WebConfigKey.PROJECT_VERSION);
        }

        CONFIG_CAHCE.put(WebConfigKey.PROJECT_VERSION, result);
        return result;
    }

    public static String getProjectBuildtime() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.PROJECT_BUILDTIME);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result;
        try {
            result = properties.get(WebConfigKey.PROJECT_BUILDTIME);
        } catch (PropertyNotFoundException e) {
            throw new RuntimeException("Couldn't load " + WebConfigKey.PROJECT_BUILDTIME);
        }

        CONFIG_CAHCE.put(WebConfigKey.PROJECT_BUILDTIME, result);
        return result;
    }

    public static String getCopyrightHolder() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.PROJECT_COPYRIGHT_HOLDER);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.PROJECT_COPYRIGHT_HOLDER,
                WebConfigDefault.PROJECT_COPYRIGHT_HOLDER);

        CONFIG_CAHCE.put(WebConfigKey.PROJECT_COPYRIGHT_HOLDER, result);
        return result;
    }

    public static String getSitename() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_SITENAME);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.WEB_SITENAME,
                WebConfigDefault.WEB_SITENAME);

        CONFIG_CAHCE.put(WebConfigKey.WEB_SITENAME, result);
        return result;
    }

    public static String getAssetsPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ASSETS_PATH);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.WEB_ASSETS_PATH,
                WebConfigDefault.WEB_ASSETS_PATH);

        CONFIG_CAHCE.put(WebConfigKey.WEB_ASSETS_PATH, result);
        return result;
    }
    
    /**
     * 检查当前配置是不是调试模式<br>
     * <b>注意:</b> 根据配置不同,调试模式可能包含多个{@link ProjectMode},并不是{@link ProjectMode#DEVELOP}
     * @return
     */
    public static boolean isDebugMode() {
        //TODO: make logdetailsmode configable
        return getProjectMode().equals(ProjectMode.DEVELOP) ||
                getProjectMode().equals(ProjectMode.DEBUG);
    }

    public static String getDefaultTheme() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_DEFAULT_THEME);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.WEB_DEFAULT_THEME,
                WebConfigDefault.WEB_DEFAULT_THEME);

        CONFIG_CAHCE.put(WebConfigKey.WEB_DEFAULT_THEME, result);
        return result;
    }

    public static String getAdminDashboardBrandIcon() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_ICON);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_ICON,
                WebConfigDefault.WEB_ADMIN_DASHBOARD_BRAND_ICON);

        CONFIG_CAHCE.put(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_ICON, result);
        return result;
    }
    
    public static String getAdminDashboardBrandText() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_TEXT);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_TEXT,
                WebConfigDefault.WEB_ADMIN_DASHBOARD_BRAND_TEXT);

        CONFIG_CAHCE.put(WebConfigKey.WEB_ADMIN_DASHBOARD_BRAND_TEXT, result);
        return result;
    }

    public static String getRootContextConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_ROOT_CONTEXT_CONFIG_CLASS);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.CORE_ROOT_CONTEXT_CONFIG_CLASS,
                WebConfigDefault.CORE_ROOT_CONTEXT_CONFIG_CLASS);

        CONFIG_CAHCE.put(WebConfigKey.CORE_ROOT_CONTEXT_CONFIG_CLASS, result);
        return result;
    }
    
    public static String getWebConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_WEB_CONFIG_CLASS);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.CORE_WEB_CONFIG_CLASS,
                WebConfigDefault.CORE_WEB_CONFIG_CLASS);

        CONFIG_CAHCE.put(WebConfigKey.CORE_WEB_CONFIG_CLASS, result);
        return result;
    }
    
    public static String getAdminWebConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_ADMIN_WEB_CONFIG_CLASS);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.CORE_ADMIN_WEB_CONFIG_CLASS,
                WebConfigDefault.CORE_ADMIN_WEB_CONFIG_CLASS);

        CONFIG_CAHCE.put(WebConfigKey.CORE_ADMIN_WEB_CONFIG_CLASS, result);
        return result;
    }
    
    public static String getApiConfigClassName() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_API_CONFIG_CLASS);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.CORE_API_CONFIG_CLASS,
                WebConfigDefault.CORE_API_CONFIG_CLASS);

        CONFIG_CAHCE.put(WebConfigKey.CORE_API_CONFIG_CLASS, result);
        return result;
    }

}
