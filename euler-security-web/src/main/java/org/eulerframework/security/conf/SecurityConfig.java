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
package org.eulerframework.security.conf;

import org.eulerframework.common.util.property.FilePropertySource;
import org.eulerframework.common.util.property.PropertyReader;
import org.eulerframework.web.config.WebConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class SecurityConfig {
    protected static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    private static PropertyReader propertyReader;

    static {
        try {
            propertyReader = new PropertyReader(new FilePropertySource(WebConfig.DEFAULT_CONFIG_FILE));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setPropertyReader(PropertyReader propertyReader) {
        SecurityConfig.propertyReader = propertyReader;
    }

    public static PropertyReader getPropertyReader() {
        return propertyReader;
    }

    public static class SecurityConfigKey {
        // [security]
        public static final String SECURITY_LOGIN_PAGE = "security.loginPage";
        public static final String SECURITY_LOGIN_PROCESSING_URL = "security.login.processingUrl";
        public static final String SECURITY_LOGIN_DEFAULT_TARGET_URL = "security.login.defaultTargetUrl";

        private static final String SECURITY_WEB_AUTHENTICATION_TYPE = "security.web.authenticationType";
        private static final String SECURITY_API_AUTHENTICATION_TYPE = "security.api.authenticationType";
        private static final String SECURITY_OAUTH_SERVER_TYPE = "security.oauth.severType";

        private static final String SECURITY_AUTHENTICATION_ENABLE_EMAIL_SIGNIN = "security.authentication.enableEmailSignin";
        private static final String SECURITY_AUTHENTICATION_ENABLE_MOBILE_SIGNIN = "security.authentication.enableMobileSignin";
        // private static final String SECURITY_AUTHENTICATION_ENABLE_USER_CAHCE = "security.authentication.enableUserCache";
        // private static final String SECURITY_AUTHENTICATION_USER_CAHCE_LIFE = "security.authentication.userCacheLife";
        private static final String SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE = "security.authentication.userContext.cacheLife";
        private static final String SECURITY_AUTHENTICATION_USER_DETAILS_CAHCE_ENABLED = "security.authentication.userDetails.cacheEnabled";
        private static final String SECURITY_AUTHENTICATION_USER_DETAILS_CAHCE_LIFE = "security.authentication.userDetails.cacheLife";

        private static final String SECURITY_SIGNUP_ENABLED = "security.signup.enabled";
        private static final String SECURITY_SIGNUP_ENABLE_CAPTCHA = "security.signup.enableCaptcha";
        private static final String SECURITY_SIGNUP_USERNAME_FORMAT = "security.signup.username.format";
        private static final String SECURITY_SIGNUP_EMAIL_FORMAT = "security.signup.email.format";
        private static final String SECURITY_SIGNUP_MOBILE_FORMAT = "security.signup.mobile.format";
        private static final String SECURITY_SIGNUP_PASSWORD_FORMAT = "security.signup.password.format";
        private static final String SECURITY_SIGNUP_PASSWORD_MIN_LENGTH = "security.signup.password.minLength";
        private static final String SECURITY_SIGNUP_AUTO_SIGNIN = "security.signup.autoSignin";
        private static final String SECURITY_SIGNUP_ENABLE_MOBILE_AUTO_SIGNUP = "security.signup.enableMobileAutoSignup";
        private static final String SECURITY_SIGNUP_ENABLE_INTERESTING_USERNAME_PREFIX = "security.signup.enableInterestingUsernamePrefix";

        private static final String SECURITY_SMSCODE_TEMPLATE_SIGNUP = "security.smscode.template.signup";
        private static final String SECURITY_SMSCODE_TEMPLATE_SIGNIN = "security.smscode.template.signin";
        private static final String SECURITY_SMSCODE_TEMPLATE_RESET_PASSWORD = "security.smscode.template.resetPassword";

        private static final String SECURITY_SMSCODE_EXPIRE_MINUTES_SIGNUP = "security.smscode.expire.minutes.signup";
        private static final String SECURITY_SMSCODE_EXPIRE_MINUTES_SIGNIN = "security.smscode.expire.minutes.signin";
        private static final String SECURITY_SMSCODE_EXPIRE_MINUTES_RESET_PASSWORD = "security.smscode.expire.minutes.resetPassword";
    }

    private static class SecurityConfigDefault {
        private static final String SECURITY_LOGIN_PAGE = "/signin";
        private static final String SECURITY_LOGIN_PROCESSING_URL = "/signin";
        private static final String SECURITY_LOGIN_DEFAULT_TARGET_URL = "/";

        private static final WebAuthenticationType SECURITY_WEB_AUTHENTICATION_TYPE = WebAuthenticationType.LOCAL;
        private static final ApiAuthenticationType SECURITY_API_AUTHENTICATION_TYPE = ApiAuthenticationType.NONE;
        private static final OAuthServerType SECURITY_OAUTH_SERVER_TYPE = OAuthServerType.NEITHER;
        private static final boolean SECURITY_AUTHENTICATION_ENABLE_EMAIL_SIGNIN = false;
        private static final boolean SECURITY_AUTHENTICATION_ENABLE_MOBILE_SIGNIN = false;
        // private static final boolean SECURITY_AUTHENTICATION_ENABLE_USER_CAHCE = false;
        // private static final long SECURITY_AUTHENTICATION_USER_CAHCE_LIFE = 0;
        private static final long SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE = 600_000L;
        private static final boolean SECURITY_AUTHENTICATION_USER_DETAILS_CAHCE_ENABLED = false;
        private static final long SECURITY_AUTHENTICATION_USER_DETAILS_CAHCE_LIFE = 10_000L;

        private static final boolean SECURITY_SIGNUP_ENABLED = true;
        private static final boolean SECURITY_SIGNUP_ENABLE_CAPTCHA = true;
        private static final boolean SECURITY_SIGNUP_ENABLE_MOBILE_AUTO_SIGNUP = false;
        private static final boolean SECURITY_SIGNUP_ENABLE_INTERESTING_USERNAME_PREFIX = true;
        private static final String SECURITY_SIGNUP_USERNAME_FORMAT = "^[A-Za-z][A-Za-z0-9_\\-\\.]+[A-Za-z0-9]$"; // 至少三位，以字母开头，中间可含有字符数字_-.,以字母或数字结尾
        private static final String SECURITY_SIGNUP_EMAIL_FORMAT = "^[A-Za-z0-9_\\-\\.]+@[a-zA-Z0-9_\\-]+(\\.[a-zA-Z0-9_\\-]+)+$"; // 可含有-_.的email
        private static final String SECURITY_SIGNUP_MOBILE_FORMAT = "^[0-9\\+][0-9\\-]+[0-9]$"; //以数字或+开头，后续可含有数字或-，以数字结束
        private static final String SECURITY_SIGNUP_PASSWORD_FORMAT = "^[\\u0021-\\u007e]+$"; // ASCII可显示非空白字符
        private static final int SECURITY_SIGNUP_PASSWORD_MIN_LENGTH = 6;
        private static final boolean SECURITY_SIGNUP_AUTO_SIGNIN = true;

        private static final String SECURITY_SMSCODE_TEMPLATE_SIGNUP = "[Euler Project] Your Sign Up SMS code is ${sms_code}, will exipre in ${expire_minutes} minutes.";
        private static final String SECURITY_SMSCODE_TEMPLATE_SIGNIN = "[Euler Project] Your Sign In SMS code is ${sms_code}, will exipre in ${expire_minutes} minutes.";
        private static final String SECURITY_SMSCODE_TEMPLATE_RESET_PASSWORD = "[Euler Project] Your reset password SMS code is ${sms_code}, will exipre in ${expire_minutes} minutes.";

        private static final int SECURITY_SMSCODE_EXPIRE_MINUTES_SIGNUP = 10;
        private static final int SECURITY_SMSCODE_EXPIRE_MINUTES_SIGNIN = 10;
        private static final int SECURITY_SMSCODE_EXPIRE_MINUTES_RESET_PASSWORD = 10;

    }

    public static String getLoginPage() {
        return propertyReader.getString(SecurityConfigKey.SECURITY_LOGIN_PAGE, SecurityConfigDefault.SECURITY_LOGIN_PAGE);
    }

    public static String getLoginProcessingUrl() {
        return propertyReader.getString(SecurityConfigKey.SECURITY_LOGIN_PROCESSING_URL, SecurityConfigDefault.SECURITY_LOGIN_PROCESSING_URL);
    }

    public static String getLoginDefaultTargetUrl() {
        return propertyReader.getString(SecurityConfigKey.SECURITY_LOGIN_DEFAULT_TARGET_URL, SecurityConfigDefault.SECURITY_LOGIN_DEFAULT_TARGET_URL);
    }

    public static WebAuthenticationType getWebAuthenticationType() {
        return propertyReader.get(SecurityConfigKey.SECURITY_WEB_AUTHENTICATION_TYPE, SecurityConfigDefault.SECURITY_WEB_AUTHENTICATION_TYPE);
    }

    public static ApiAuthenticationType getApiAuthenticationType() {
        return propertyReader.get(SecurityConfigKey.SECURITY_API_AUTHENTICATION_TYPE, SecurityConfigDefault.SECURITY_API_AUTHENTICATION_TYPE);
    }

    public static OAuthServerType getOAuthSeverType() {
        return propertyReader.get(SecurityConfigKey.SECURITY_OAUTH_SERVER_TYPE, SecurityConfigDefault.SECURITY_OAUTH_SERVER_TYPE);
    }

    public static int getMinPasswordLength() {
        int result = propertyReader.getIntValue(SecurityConfigKey.SECURITY_SIGNUP_PASSWORD_MIN_LENGTH,
                SecurityConfigDefault.SECURITY_SIGNUP_PASSWORD_MIN_LENGTH);

        if (result > getMaxPasswordLength()) {
            result = getMaxPasswordLength();
            LOGGER.warn("Password length must less than " + result + ", use " + result + " as "
                    + SecurityConfigKey.SECURITY_SIGNUP_PASSWORD_MIN_LENGTH);
        }

        return result;
    }

    public static int getMaxPasswordLength() {
        return 20;
    }

    public static String getUsernameFormat() {
        return propertyReader.getString(SecurityConfigKey.SECURITY_SIGNUP_USERNAME_FORMAT, SecurityConfigDefault.SECURITY_SIGNUP_USERNAME_FORMAT);
    }

    public static String getEmailFormat() {
        return propertyReader.getString(SecurityConfigKey.SECURITY_SIGNUP_EMAIL_FORMAT, SecurityConfigDefault.SECURITY_SIGNUP_EMAIL_FORMAT);
    }

    public static String getMobileFormat() {
        return propertyReader.getString(SecurityConfigKey.SECURITY_SIGNUP_MOBILE_FORMAT, SecurityConfigDefault.SECURITY_SIGNUP_MOBILE_FORMAT);
    }

    public static String getPasswordFormat() {
        return propertyReader.getString(SecurityConfigKey.SECURITY_SIGNUP_PASSWORD_FORMAT, SecurityConfigDefault.SECURITY_SIGNUP_PASSWORD_FORMAT);
    }

    public static boolean isEnableEmailSignin() {
        return propertyReader.getBooleanValue(SecurityConfigKey.SECURITY_AUTHENTICATION_ENABLE_EMAIL_SIGNIN, SecurityConfigDefault.SECURITY_AUTHENTICATION_ENABLE_EMAIL_SIGNIN);
    }

    public static boolean isEnableMobileSignin() {
        return propertyReader.getBooleanValue(SecurityConfigKey.SECURITY_AUTHENTICATION_ENABLE_MOBILE_SIGNIN, SecurityConfigDefault.SECURITY_AUTHENTICATION_ENABLE_MOBILE_SIGNIN);
    }

    public static boolean isEnableUserDetailsCache() {
        return propertyReader.getBooleanValue(SecurityConfigKey.SECURITY_AUTHENTICATION_USER_DETAILS_CAHCE_ENABLED, SecurityConfigDefault.SECURITY_AUTHENTICATION_USER_DETAILS_CAHCE_ENABLED);
    }

    public static long getUserDetailsCacheLife() {
        return propertyReader.getLongValue(SecurityConfigKey.SECURITY_AUTHENTICATION_USER_DETAILS_CAHCE_LIFE,
                SecurityConfigDefault.SECURITY_AUTHENTICATION_USER_DETAILS_CAHCE_LIFE);
    }

    public static long getUserContextCacheLife() {
        return propertyReader.getLongValue(SecurityConfigKey.SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE,
                SecurityConfigDefault.SECURITY_AUTHENTICATION_USERCONTEXT_CAHCE_LIFE);
    }

    public static boolean isEnableAutoSigninAfterSignup() {
        return propertyReader.getBooleanValue(SecurityConfigKey.SECURITY_SIGNUP_AUTO_SIGNIN, SecurityConfigDefault.SECURITY_SIGNUP_AUTO_SIGNIN);
    }

    public static boolean isSignUpEnabled() {
        return propertyReader.getBooleanValue(SecurityConfigKey.SECURITY_SIGNUP_ENABLED,
                SecurityConfigDefault.SECURITY_SIGNUP_ENABLED);
    }

    public static boolean isSignUpEnableCaptcha() {
        return propertyReader.getBooleanValue(SecurityConfigKey.SECURITY_SIGNUP_ENABLE_CAPTCHA,
                SecurityConfigDefault.SECURITY_SIGNUP_ENABLE_CAPTCHA);
    }

    public static String getSmsCodeTemplateSignUp() {
        return propertyReader.getString(SecurityConfigKey.SECURITY_SMSCODE_TEMPLATE_SIGNUP,
                SecurityConfigDefault.SECURITY_SMSCODE_TEMPLATE_SIGNUP);
    }

    public static String getSmsCodeTemplateSignIn() {
        return propertyReader.getString(SecurityConfigKey.SECURITY_SMSCODE_TEMPLATE_SIGNIN,
                SecurityConfigDefault.SECURITY_SMSCODE_TEMPLATE_SIGNIN);
    }

    public static String getSmsCodeTemplateResetPassword() {
        return propertyReader.getString(SecurityConfigKey.SECURITY_SMSCODE_TEMPLATE_RESET_PASSWORD,
                SecurityConfigDefault.SECURITY_SMSCODE_TEMPLATE_RESET_PASSWORD);
    }

    public static int getSmsCodeExpireMinutesSignUp() {
        return propertyReader.getIntValue(SecurityConfigKey.SECURITY_SMSCODE_EXPIRE_MINUTES_SIGNUP,
                SecurityConfigDefault.SECURITY_SMSCODE_EXPIRE_MINUTES_SIGNUP);
    }

    public static int getSmsCodeExpireMinutesSignIn() {
        return propertyReader.getIntValue(SecurityConfigKey.SECURITY_SMSCODE_EXPIRE_MINUTES_SIGNIN,
                SecurityConfigDefault.SECURITY_SMSCODE_EXPIRE_MINUTES_SIGNIN);
    }

    public static int getSmsCodeExpireMinutesResetPassword() {
        return propertyReader.getIntValue(SecurityConfigKey.SECURITY_SMSCODE_EXPIRE_MINUTES_RESET_PASSWORD,
                SecurityConfigDefault.SECURITY_SMSCODE_EXPIRE_MINUTES_RESET_PASSWORD);
    }

    public static boolean isEnableAutoAuthorizeAfterSignup() {
        // TODO Auto-generated method stub
        return false;
    }

    public static String[] getAutoAuthorizeGroupId() {
        // TODO Auto-generated method stub
        return new String[]{"8a775fcf-6f3e-4b57-8a1a-a9bd96a4bf49"};
    }

    /**
     * @return
     */
    public static boolean isEnableMobileAutoSignup() {
        return propertyReader.getBooleanValue(SecurityConfigKey.SECURITY_SIGNUP_ENABLE_MOBILE_AUTO_SIGNUP,
                SecurityConfigDefault.SECURITY_SIGNUP_ENABLE_MOBILE_AUTO_SIGNUP);
    }

    /**
     * @return
     */
    public static boolean isEnableInterestingRandomUsernamePrefix() {
        return propertyReader.getBooleanValue(SecurityConfigKey.SECURITY_SIGNUP_ENABLE_INTERESTING_USERNAME_PREFIX,
                SecurityConfigDefault.SECURITY_SIGNUP_ENABLE_INTERESTING_USERNAME_PREFIX);
    }

}
