package org.eulerframework.security.oauth2.core;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

public class EulerAuthorizationGrantType {
    public static final AuthorizationGrantType WECHAT_LOGIN_CODE = new AuthorizationGrantType("wechat_login_code");
}
