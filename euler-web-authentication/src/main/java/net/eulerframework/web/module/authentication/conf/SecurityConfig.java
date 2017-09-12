package net.eulerframework.web.module.authentication.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import net.eulerframework.cache.inMemoryCache.AbstractObjectCache.DataGetter;
import net.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import net.eulerframework.cache.inMemoryCache.ObjectCachePool;
import net.eulerframework.common.util.property.PropertyReader;

@Configuration
public abstract class SecurityConfig {
    protected static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    private static final DefaultObjectCache<String, Object> CONFIG_CAHCE = ObjectCachePool
            .generateDefaultObjectCache(Long.MAX_VALUE);

    private static final PropertyReader properties = new PropertyReader("/config.properties");

    private static class WebConfigKey {
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

    public static boolean clearSecurityConfigCache() {
        properties.refresh();
        return CONFIG_CAHCE.clear();
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
        Object cachedConfig = CONFIG_CAHCE.get(WebConfigKey.SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE, configKey -> 
            properties.getLongValue(configKey,
                    WebConfigDefault.SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE)
        );

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

}
