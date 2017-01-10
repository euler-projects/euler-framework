package net.eulerframework.web.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.ContextLoader;

import net.eulerframework.cache.DefaultObjectCache;
import net.eulerframework.cache.ObjectCachePool;
import net.eulerframework.common.util.FilePathTool;
import net.eulerframework.common.util.GlobalProperties1;
import net.eulerframework.common.util.GlobalPropertyReadException;
import net.eulerframework.common.util.StringTool;

public abstract class WebConfig {

    private final static DefaultObjectCache<String, Object> CONFIG_CAHCE = ObjectCachePool
            .generateDefaultObjectCache(Long.MAX_VALUE);

    private static class WebConfigKey {
        //[core]
        private final static String CORE_I18N_REFRESH_FREQ = "core.i18n.refreshFreq";
        private final static String CORE_CACHE_RAM_CAHCE_REFRESH_FREQ = "core.cache.refreshFreq";
        private final static String CORE_CACHE_USERCONTEXT_CAHCE_LIFE = "core.cache.userContextCacheLife";

        //[web]
        private final static String WEB_UPLOAD_PATH = "web.uploadPath";
        private final static String WEB_JSP_PATH = "web.jspPath";
        private final static String WEB_ADMIN_JSP_PATH = "web.admin.JspPath";
        private final static String WEB_ADMIN_ROOT_PATH = "web.admin.rootPath";
        private final static String WEB_API_ROOT_PATH = "web.api.rootPath";
        
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
        private static final String SEC_MIN_PASSWORD_LENGTH = "sec.minPasswordLength";
    }

    private static class WebConfigDefault {
        private final static int CORE_I18N_REFRESH_FREQ = 86_400;
        private final static long CORE_CACHE_RAM_CAHCE_REFRESH_FREQ = 60_000L;
        private final static long CORE_CACHE_USERCONTEXT_CAHCE_LIFE = 600_000L;

        private final static String WEB_UPLOAD_PATH_UNIX = "file:///var/lib/euler-framework/archive/files";
        private final static String WEB_UPLOAD_PATH_WIN = "file://C:\\euler-framework-data\\archive\files";
        private final static String WEB_JSP_PATH = "/WEB-INF/jsp/themes";
        private final static String WEB_ADMIN_JSP_PATH = "/WEB-INF/jsp/admin/themes";
        private final static String WEB_ADMIN_ROOT_PATH = "/admin";
        
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
        private static final int SEC_MIN_PASSWORD_LENGTH = 6;
    }

    protected final static Logger log = LogManager.getLogger();

    public static boolean clearWebConfigCache() {
        GlobalProperties1.refresh();
        return CONFIG_CAHCE.clear();
    }

    public static int getI18nRefreshFreq() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_I18N_REFRESH_FREQ);
        if (cachedConfig != null) {
            return (int) cachedConfig;
        }

        int result = GlobalProperties1.getIntValue(WebConfigKey.CORE_I18N_REFRESH_FREQ,
                WebConfigDefault.CORE_I18N_REFRESH_FREQ);

        CONFIG_CAHCE.put(WebConfigKey.CORE_I18N_REFRESH_FREQ, result);
        return result;
    }

    public static WebAuthenticationType getWebAuthenticationType() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_WEB_AUTHENTICATION_TYPE);
        if (cachedConfig != null) {
            return (WebAuthenticationType) cachedConfig;
        }

        WebAuthenticationType result = GlobalProperties1.getEnumValue(WebConfigKey.SEC_WEB_AUTHENTICATION_TYPE,
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

        ApiAuthenticationType result = GlobalProperties1.getEnumValue(WebConfigKey.SEC_API_AUTHENTICATION_TYPE,
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

        OAuthServerType result = GlobalProperties1.getEnumValue(WebConfigKey.SEC_OAUTH_SERVER_TYPE,
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
            result = GlobalProperties1.get(WebConfigKey.WEB_API_ROOT_PATH);

            if (StringTool.isNull(result))
                throw new RuntimeException(WebConfigKey.WEB_API_ROOT_PATH + "can not be empty");

            while (result.endsWith("*")) {
                result = result.substring(0, result.length() - 1);
            }

            result = FilePathTool.changeToUnixFormat(result);

            if (!result.startsWith("/"))
                result = "/" + result;

        } catch (GlobalPropertyReadException e) {
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

        String result = GlobalProperties1.get(WebConfigKey.WEB_ADMIN_ROOT_PATH, WebConfigDefault.WEB_ADMIN_ROOT_PATH);

        if (StringTool.isNull(result))
            throw new RuntimeException(WebConfigKey.WEB_ADMIN_ROOT_PATH + "can not be empty");

        while (result.endsWith("*")) {
            result = result.substring(0, result.length() - 1);
        }

        result = FilePathTool.changeToUnixFormat(result);

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
            result = FilePathTool.changeToUnixFormat(GlobalProperties1.get(WebConfigKey.WEB_UPLOAD_PATH));
        } catch (GlobalPropertyReadException e) {
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

        String result = FilePathTool
                .changeToUnixFormat(GlobalProperties1.get(WebConfigKey.WEB_JSP_PATH, WebConfigDefault.WEB_JSP_PATH));
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

        String result = FilePathTool.changeToUnixFormat(
                GlobalProperties1.get(WebConfigKey.WEB_ADMIN_JSP_PATH, WebConfigDefault.WEB_ADMIN_JSP_PATH));
        // 统一添加/结尾，这样在controller中就可以不加/前缀
        result = result + "/";

        CONFIG_CAHCE.put(WebConfigKey.WEB_ADMIN_JSP_PATH, result);
        return result;
    }

    public static long getRamCacheCleanFreq() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_CACHE_RAM_CAHCE_REFRESH_FREQ);
        if (cachedConfig != null) {
            return (long) cachedConfig;
        }

        long result = GlobalProperties1.getLongValue(WebConfigKey.CORE_CACHE_RAM_CAHCE_REFRESH_FREQ,
                WebConfigDefault.CORE_CACHE_RAM_CAHCE_REFRESH_FREQ);

        CONFIG_CAHCE.put(WebConfigKey.CORE_CACHE_RAM_CAHCE_REFRESH_FREQ, result);
        return result;
    }

    public static long getUserContextCacheLife() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.CORE_CACHE_USERCONTEXT_CAHCE_LIFE);
        if (cachedConfig != null) {
            return (long) cachedConfig;
        }

        long result = GlobalProperties1.getLongValue(WebConfigKey.CORE_CACHE_USERCONTEXT_CAHCE_LIFE,
                WebConfigDefault.CORE_CACHE_USERCONTEXT_CAHCE_LIFE);

        CONFIG_CAHCE.put(WebConfigKey.CORE_CACHE_USERCONTEXT_CAHCE_LIFE, result);
        return result;
    }

    public static MultiPartConfig getMultiPartConfig() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.WEB_MULITPART);
        if (cachedConfig != null) {
            return (MultiPartConfig) cachedConfig;
        }

        String location = GlobalProperties1.get(WebConfigKey.WEB_MULITPART_LOCATION,
                WebConfigDefault.WEB_MULITPART_LOCATION);
        long maxFileSize = GlobalProperties1.getLongValue(WebConfigKey.WEB_MULITPART_MAX_FILE_SIZE,
                WebConfigDefault.WEB_MULITPART_MAX_FILE_SIZE);
        long maxRequestSize = GlobalProperties1.getLongValue(WebConfigKey.WEB_MULITPART_MAX_REQUEST_SIZE,
                WebConfigDefault.WEB_MULITPART_MAX_REQUEST_SIZE);
        int fileSizeThreshold = GlobalProperties1.getIntValue(WebConfigKey.WEB_MULITPART_FILE_SIZE_THRESHOLD,
                WebConfigDefault.WEB_MULITPART_FILE_SIZE_THRESHOLD);

        MultiPartConfig result = new MultiPartConfig(location, maxFileSize, maxRequestSize, fileSizeThreshold);

        CONFIG_CAHCE.put(WebConfigKey.WEB_MULITPART, result);
        return result;
    }

    public static int getMinPasswordLength() {

        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_MIN_PASSWORD_LENGTH);
        if (cachedConfig != null) {
            return (int) cachedConfig;
        }

        int result = GlobalProperties1.getIntValue(WebConfigKey.SEC_MIN_PASSWORD_LENGTH,
                WebConfigDefault.SEC_MIN_PASSWORD_LENGTH);

        CONFIG_CAHCE.put(WebConfigKey.SEC_MIN_PASSWORD_LENGTH, result);
        return result;
    }

    public static String getSignUpSuccessPage() {
        // TODO Auto-generated method stub
        return "signupSuccess";
    }

    public static String getSignUpFailPage() {
        // TODO Auto-generated method stub
        return "signupFail";
    }

    public static String getUsernameFormat() {
        // TODO Auto-generated method stub
        return "^[A-Za-z0-9][A-Za-z0-9_\\-\\.]+[A-Za-z0-9]$";
    }

    public static String getEmailFormat() {
        // TODO Auto-generated method stub
        return "^[A-Za-z0-9_\\-\\.]+@[a-zA-Z0-9_\\-]+(\\.[a-zA-Z0-9_\\-]+)+$";
    }

    public static String getPasswordFormat() {
        // TODO Auto-generated method stub
        return "^[\\u0021-\\u007e]+$";
    }

    public static void main(String[] args) {
        System.out.println(ApiAuthenticationType.BASIC.getDeclaringClass());
        // System.out.println(WebConfig.getAdminJspPath());
        // System.out.println(WebConfig.getAdminRootPath());
        // System.out.println(WebConfig.getApiAuthenticationType());
        // System.out.println(WebConfig.getApiRootPath());
        // System.out.println(WebConfig.getEmailFormat());
        // System.out.println(WebConfig.getI18nRefreshFreq());
        // System.out.println(WebConfig.getJspPath());
        // System.out.println(WebConfig.getMinPasswordLength());
        // System.out.println(WebConfig.getMultiPartConfig());
        // System.out.println(WebConfig.getOAuthSeverType());
        // System.out.println(WebConfig.getPasswordFormat());
        // System.out.println(WebConfig.getRamCacheCleanFreq());
        // System.out.println(WebConfig.getSignUpFailPage());
        // System.out.println(WebConfig.getSignUpSuccessPage());
        // System.out.println(WebConfig.getUploadPath());
        // System.out.println(WebConfig.getUserContextCacheLife());
        // System.out.println(WebConfig.getUsernameFormat());
        // System.out.println(WebConfig.getWebAuthenticationType());
    }

    public static boolean isEnableEmailSignin() {

        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_AUTHENTICATION_ENABLE_EMAIL_SIGNIN);
        if (cachedConfig != null) {
            return (boolean) cachedConfig;
        }

        boolean result = GlobalProperties1.getBooleanValue(WebConfigKey.SEC_AUTHENTICATION_ENABLE_EMAIL_SIGNIN,
                WebConfigDefault.SEC_AUTHENTICATION_ENABLE_EMAIL_SIGNIN);

        CONFIG_CAHCE.put(WebConfigKey.SEC_AUTHENTICATION_ENABLE_EMAIL_SIGNIN, result);
        return result;
    }

    public static boolean isEnableMobileSignin() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_AUTHENTICATION_ENABLE_MOBILE_SIGNIN);
        if (cachedConfig != null) {
            return (boolean) cachedConfig;
        }

        boolean result = GlobalProperties1.getBooleanValue(WebConfigKey.SEC_AUTHENTICATION_ENABLE_MOBILE_SIGNIN,
                WebConfigDefault.SEC_AUTHENTICATION_ENABLE_MOBILE_SIGNIN);

        CONFIG_CAHCE.put(WebConfigKey.SEC_AUTHENTICATION_ENABLE_MOBILE_SIGNIN, result);
        return result;
    }

    public static boolean isEnableUserCache() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_AUTHENTICATION_ENABLE_USER_CAHCE);
        if (cachedConfig != null) {
            return (boolean) cachedConfig;
        }

        boolean result = GlobalProperties1.getBooleanValue(WebConfigKey.SEC_AUTHENTICATION_ENABLE_USER_CAHCE,
                WebConfigDefault.SEC_AUTHENTICATION_ENABLE_USER_CAHCE);

        CONFIG_CAHCE.put(WebConfigKey.SEC_AUTHENTICATION_ENABLE_USER_CAHCE, result);
        return result;
    }

    public static long getUserAuthenticationCacheLife() {
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SEC_AUTHENTICATION_USER_CAHCE_LIFE);
        if (cachedConfig != null) {
            return (long) cachedConfig;
        }

        long result = GlobalProperties1.getLongValue(WebConfigKey.SEC_AUTHENTICATION_USER_CAHCE_LIFE,
                WebConfigDefault.SEC_AUTHENTICATION_USER_CAHCE_LIFE);

        CONFIG_CAHCE.put(WebConfigKey.SEC_AUTHENTICATION_USER_CAHCE_LIFE, result);
        return result;
    }

}
