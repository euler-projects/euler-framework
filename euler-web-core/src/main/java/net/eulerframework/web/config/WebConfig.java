package net.eulerframework.web.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.ContextLoader;

import net.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import net.eulerframework.cache.inMemoryCache.ObjectCachePool;
import net.eulerframework.common.util.FileReader;
import net.eulerframework.common.util.PropertyReader;
import net.eulerframework.common.util.StringTool;
import net.eulerframework.common.util.exception.PropertyReadException;

public abstract class WebConfig {

    private final static DefaultObjectCache<String, Object> CONFIG_CAHCE = ObjectCachePool
            .generateDefaultObjectCache(Long.MAX_VALUE);
    
    private final static PropertyReader properties = new PropertyReader("/config.properties");

    private static class WebConfigKey {
        //[project]
        private final static String PROJECT_SITENAME = "project.sitename";
        private final static String PROJECT_VERSION = "project.verison";
        private final static String PROJECT_MODE = "project.mode";
        private final static String PROJECT_BUILDTIME = "project.buildtime";
        private static final String PROJECT_COPYRIGHT_HOLDER = "project.copyrightHolder";
        
        //[core]
        private final static String CORE_CACHE_I18N_REFRESH_FREQ = "core.cache.i18n.refreshFreq";
        private final static String CORE_CAHCE_RAMCACHE_POOL_CLEAN_FREQ = "core.cache.ramCachePool.cleanFreq";
        private final static String CORE_CACHE_USERCONTEXT_CAHCE_LIFE = "core.cache.userContext.cacheLife";

        //[web]
        private final static String WEB_UPLOAD_PATH = "web.uploadPath";
        private final static String WEB_JSP_PATH = "web.jspPath";
        private final static String WEB_ADMIN_JSP_PATH = "web.admin.JspPath";
        private final static String WEB_ADMIN_ROOT_PATH = "web.admin.rootPath";
        private final static String WEB_API_ROOT_PATH = "web.api.rootPath";
        private static final String WEB_ASSETS_PATH = "web.asstesPath";
        
        private final static String WEB_MULITPART = "web.multipart";
        private static final String WEB_MULITPART_LOCATION = "web.multiPart.location";
        private static final String WEB_MULITPART_MAX_FILE_SIZE = "web.multiPart.maxFileSize";
        private static final String WEB_MULITPART_MAX_REQUEST_SIZE = "web.multiPart.maxRequestSize";
        private static final String WEB_MULITPART_FILE_SIZE_THRESHOLD = "web.multiPart.fileSizeThreshold";

        //[security]
        private static final String SEC_WEB_AUTHENTICATION_TYPE = "sec.web.authenticationType";
        private static final String SEC_API_AUTHENTICATION_TYPE = "sec.api.authenticationType";
        private static final String SEC_OAUTH_SERVER_TYPE = "sec.oauth.severType";
        
        private static final String SEC_AUTHENTICATION_ENABLE_EMAIL_SIGNIN = "sec.authentication.enableEmailSignin";
        private static final String SEC_AUTHENTICATION_ENABLE_MOBILE_SIGNIN = "sec.authentication.enableMobileSignin";
        private static final String SEC_AUTHENTICATION_ENABLE_USER_CAHCE = "sec.authentication.enableUserCache";
        private static final String SEC_AUTHENTICATION_USER_CAHCE_LIFE = "sec.authentication.userCacheLife";

        private static final String SEC_SIGNUP_USERNAME_FORMAT = "sec.signup.username.format";
        private static final String SEC_SIGNUP_EMAIL_FORMAT = "sec.signup.email.format";
        private static final String SEC_SIGNUP_PASSWORD_FORMAT = "sec.signup.password.format";
        private static final String SEC_SIGNUP_PASSWORD_MIN_LENGTH = "sec.signup.password.minLength";
        private static final String SEC_SIGNUP_AUTO_SIGNIN = "sec.signup.autoSignin";
    }

    private static class WebConfigDefault {
        private final static String PROJECT_SITENAME = "DEMO";
        private static final String PROJECT_COPYRIGHT_HOLDER = "Euler Projects";
        private static final ProjectMode PROJECT_MODE = ProjectMode.DEBUG;
        
        private final static int CORE_CACHE_I18N_REFRESH_FREQ = 86_400;
        private final static long CORE_CAHCE_RAMCACHE_POOL_CLEAN_FREQ = 60_000L;
        private final static long CORE_CACHE_USERCONTEXT_CAHCE_LIFE = 600_000L;

        private final static String WEB_UPLOAD_PATH_UNIX = "file:///var/lib/euler-framework/archive/files";
        private final static String WEB_UPLOAD_PATH_WIN = "file://C:\\euler-framework-data\\archive\files";
        private final static String WEB_JSP_PATH = "/WEB-INF/jsp/themes";
        private final static String WEB_ADMIN_JSP_PATH = "/WEB-INF/jsp/admin/themes";
        private final static String WEB_ADMIN_ROOT_PATH = "/admin";
        private static final String WEB_ASSETS_PATH = "/assets";
        
        private static final String WEB_MULITPART_LOCATION = null;
        private static final long WEB_MULITPART_MAX_FILE_SIZE = 51_200L;
        private static final long WEB_MULITPART_MAX_REQUEST_SIZE = 51_200L;
        private static final int WEB_MULITPART_FILE_SIZE_THRESHOLD = 1_024;

        private static final WebAuthenticationType SEC_WEB_AUTHENTICATION_TYPE = WebAuthenticationType.LOCAL;
        private static final ApiAuthenticationType SEC_API_AUTHENTICATION_TYPE = ApiAuthenticationType.NONE;
        private static final OAuthServerType SEC_OAUTH_SERVER_TYPE = OAuthServerType.NEITHER;
        private static final boolean SEC_AUTHENTICATION_ENABLE_EMAIL_SIGNIN = false;
        private static final boolean SEC_AUTHENTICATION_ENABLE_MOBILE_SIGNIN = false;
        private static final boolean SEC_AUTHENTICATION_ENABLE_USER_CAHCE = false;
        private static final long SEC_AUTHENTICATION_USER_CAHCE_LIFE = 0;

        private static final String SEC_SIGNUP_USERNAME_FORMAT = "^[A-Za-z][A-Za-z0-9_\\-\\.]+[A-Za-z0-9]$"; //至少三位，以字母开头，中间可含有字符数字_-.,以字母或数字结尾
        private static final String SEC_SIGNUP_EMAIL_FORMAT = "^[A-Za-z0-9_\\-\\.]+@[a-zA-Z0-9_\\-]+(\\.[a-zA-Z0-9_\\-]+)+$"; //可含有-_.的email
        private static final String SEC_SIGNUP_PASSWORD_FORMAT = "^[\\u0021-\\u007e]+$"; //ASCII可显示非空白字符
        private static final int SEC_SIGNUP_PASSWORD_MIN_LENGTH = 6;
        private static final boolean SEC_SIGNUP_AUTO_SIGNIN = true;

    }

    protected final static Logger log = LogManager.getLogger();

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
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_WEB_AUTHENTICATION_TYPE);
        if (cachedConfig != null) {
            return (WebAuthenticationType) cachedConfig;
        }

        WebAuthenticationType result = properties.getEnumValue(WebConfigKey.SEC_WEB_AUTHENTICATION_TYPE,
                WebConfigDefault.SEC_WEB_AUTHENTICATION_TYPE,
                true);

        CONFIG_CAHCE.put(WebConfigKey.SEC_WEB_AUTHENTICATION_TYPE, result);
        return result;
    }

    public static ApiAuthenticationType getApiAuthenticationType() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_API_AUTHENTICATION_TYPE);
        if (cachedConfig != null) {
            return (ApiAuthenticationType) cachedConfig;
        }

        ApiAuthenticationType result = properties.getEnumValue(WebConfigKey.SEC_API_AUTHENTICATION_TYPE,
                WebConfigDefault.SEC_API_AUTHENTICATION_TYPE,
                true);

        CONFIG_CAHCE.put(WebConfigKey.SEC_API_AUTHENTICATION_TYPE, result);
        return result;
    }

    public static OAuthServerType getOAuthSeverType() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_OAUTH_SERVER_TYPE);
        if (cachedConfig != null) {
            return (OAuthServerType) cachedConfig;
        }

        OAuthServerType result = properties.getEnumValue(WebConfigKey.SEC_OAUTH_SERVER_TYPE,
                WebConfigDefault.SEC_OAUTH_SERVER_TYPE,
                true);

        CONFIG_CAHCE.put(WebConfigKey.SEC_OAUTH_SERVER_TYPE, result);
        return result;
    }

    public static String getApiRootPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_API_ROOT_PATH);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result;
        try {
            result = properties.get(WebConfigKey.WEB_API_ROOT_PATH);

            if (StringTool.isNull(result))
                throw new RuntimeException(WebConfigKey.WEB_API_ROOT_PATH + "can not be empty");

            while (result.endsWith("*")) {
                result = result.substring(0, result.length() - 1);
            }

            result = FileReader.changeToUnixFormat(result);

            if (!result.startsWith("/"))
                result = "/" + result;

        } catch (PropertyReadException e) {
            throw new RuntimeException("Couldn't load " + WebConfigKey.WEB_API_ROOT_PATH);
        }

        CONFIG_CAHCE.put(WebConfigKey.WEB_API_ROOT_PATH, result);
        return result;

    }

    public static String getAdminRootPath() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_ADMIN_ROOT_PATH);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.WEB_ADMIN_ROOT_PATH, WebConfigDefault.WEB_ADMIN_ROOT_PATH);

        if (StringTool.isNull(result))
            throw new RuntimeException(WebConfigKey.WEB_ADMIN_ROOT_PATH + "can not be empty");

        while (result.endsWith("*")) {
            result = result.substring(0, result.length() - 1);
        }

        result = FileReader.changeToUnixFormat(result);

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
            result = FileReader.changeToUnixFormat(properties.get(WebConfigKey.WEB_UPLOAD_PATH));
        } catch (PropertyReadException e) {
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

        String result = FileReader
                .changeToUnixFormat(properties.get(WebConfigKey.WEB_JSP_PATH, WebConfigDefault.WEB_JSP_PATH));
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

        String result = FileReader.changeToUnixFormat(
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

    public static long getUserContextCacheLife() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_CACHE_USERCONTEXT_CAHCE_LIFE);
        if (cachedConfig != null) {
            return (long) cachedConfig;
        }

        long result = properties.getLongValue(WebConfigKey.CORE_CACHE_USERCONTEXT_CAHCE_LIFE,
                WebConfigDefault.CORE_CACHE_USERCONTEXT_CAHCE_LIFE);

        CONFIG_CAHCE.put(WebConfigKey.CORE_CACHE_USERCONTEXT_CAHCE_LIFE, result);
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

        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_SIGNUP_PASSWORD_MIN_LENGTH);
        if (cachedConfig != null) {
            return (int) cachedConfig;
        }

        int result = properties.getIntValue(WebConfigKey.SEC_SIGNUP_PASSWORD_MIN_LENGTH,
                WebConfigDefault.SEC_SIGNUP_PASSWORD_MIN_LENGTH);

        CONFIG_CAHCE.put(WebConfigKey.SEC_SIGNUP_PASSWORD_MIN_LENGTH, result);
        return result;
    }

    public static String getUsernameFormat() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_SIGNUP_USERNAME_FORMAT);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.SEC_SIGNUP_USERNAME_FORMAT,
                WebConfigDefault.SEC_SIGNUP_USERNAME_FORMAT);

        CONFIG_CAHCE.put(WebConfigKey.SEC_SIGNUP_USERNAME_FORMAT, result);
        return result;
    }

    public static String getEmailFormat() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_SIGNUP_EMAIL_FORMAT);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.SEC_SIGNUP_EMAIL_FORMAT,
                WebConfigDefault.SEC_SIGNUP_EMAIL_FORMAT);

        CONFIG_CAHCE.put(WebConfigKey.SEC_SIGNUP_EMAIL_FORMAT, result);
        return result;
    }

    public static String getPasswordFormat() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_SIGNUP_PASSWORD_FORMAT);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.SEC_SIGNUP_PASSWORD_FORMAT,
                WebConfigDefault.SEC_SIGNUP_PASSWORD_FORMAT);

        CONFIG_CAHCE.put(WebConfigKey.SEC_SIGNUP_PASSWORD_FORMAT, result);
        return result;
    }

    public static boolean isEnableEmailSignin() {

        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_AUTHENTICATION_ENABLE_EMAIL_SIGNIN);
        if (cachedConfig != null) {
            return (boolean) cachedConfig;
        }

        boolean result = properties.getBooleanValue(WebConfigKey.SEC_AUTHENTICATION_ENABLE_EMAIL_SIGNIN,
                WebConfigDefault.SEC_AUTHENTICATION_ENABLE_EMAIL_SIGNIN);

        CONFIG_CAHCE.put(WebConfigKey.SEC_AUTHENTICATION_ENABLE_EMAIL_SIGNIN, result);
        return result;
    }

    public static boolean isEnableMobileSignin() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_AUTHENTICATION_ENABLE_MOBILE_SIGNIN);
        if (cachedConfig != null) {
            return (boolean) cachedConfig;
        }

        boolean result = properties.getBooleanValue(WebConfigKey.SEC_AUTHENTICATION_ENABLE_MOBILE_SIGNIN,
                WebConfigDefault.SEC_AUTHENTICATION_ENABLE_MOBILE_SIGNIN);

        CONFIG_CAHCE.put(WebConfigKey.SEC_AUTHENTICATION_ENABLE_MOBILE_SIGNIN, result);
        return result;
    }

    public static boolean isEnableUserCache() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_AUTHENTICATION_ENABLE_USER_CAHCE);
        if (cachedConfig != null) {
            return (boolean) cachedConfig;
        }

        boolean result = properties.getBooleanValue(WebConfigKey.SEC_AUTHENTICATION_ENABLE_USER_CAHCE,
                WebConfigDefault.SEC_AUTHENTICATION_ENABLE_USER_CAHCE);

        CONFIG_CAHCE.put(WebConfigKey.SEC_AUTHENTICATION_ENABLE_USER_CAHCE, result);
        return result;
    }

    public static long getUserAuthenticationCacheLife() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_AUTHENTICATION_USER_CAHCE_LIFE);
        if (cachedConfig != null) {
            return (long) cachedConfig;
        }

        long result = properties.getLongValue(WebConfigKey.SEC_AUTHENTICATION_USER_CAHCE_LIFE,
                WebConfigDefault.SEC_AUTHENTICATION_USER_CAHCE_LIFE);

        CONFIG_CAHCE.put(WebConfigKey.SEC_AUTHENTICATION_USER_CAHCE_LIFE, result);
        return result;
    }

    public static boolean getAutoAuthorization() {
        // TODO Auto-generated method stub
        return true;
    }

    public static String[] getAutoAuthorizationId() {
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
        } catch (PropertyReadException e) {
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
        } catch (PropertyReadException e) {
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
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.PROJECT_SITENAME);
        if (cachedConfig != null) {
            return (String) cachedConfig;
        }

        String result = properties.get(WebConfigKey.PROJECT_SITENAME,
                WebConfigDefault.PROJECT_SITENAME);

        CONFIG_CAHCE.put(WebConfigKey.PROJECT_SITENAME, result);
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
    
    public static boolean isLogDetailsMode() {
        //TODO: make logdetailsmode configable
        return getProjectMode().equals(ProjectMode.DEVELOP) ||
                getProjectMode().equals(ProjectMode.DEBUG);
    }

    public static boolean getAutoSigninAfterSignup() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_SIGNUP_AUTO_SIGNIN);
        if (cachedConfig != null) {
            return (boolean) cachedConfig;
        }

        boolean result = properties.getBooleanValue(WebConfigKey.SEC_SIGNUP_AUTO_SIGNIN,
                WebConfigDefault.SEC_SIGNUP_AUTO_SIGNIN);

        CONFIG_CAHCE.put(WebConfigKey.SEC_SIGNUP_AUTO_SIGNIN, result);
        return result;
    }

}
