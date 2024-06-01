package org.eulerframework.security.web.endpoint;

public abstract class EulerSecurityEndpoints {
    public static final String SIGNUP_ENABLED_PROPERTY_NAME = "euler.security.web.signup.enabled";
    public static final String SIGNUP_PAGE_PROPERTY_NAME = "euler.security.web.signup-page";
    public static final String LOGIN_PAGE_PROPERTY_NAME = "euler.security.web.login-page";
    public static final String LOGOUT_PAGE_PROPERTY_NAME = "euler.security.web.logout-page";
    public static final String SIGNUP_PROCESSING_URL_PROPERTY_NAME = "euler.security.web.signup-processing-url";
    public static final String LOGIN_PROCESSING_URL_PROPERTY_NAME = "euler.security.web.login-processing-url";
    public static final String LOGOUT_PROCESSING_URL_PROPERTY_NAME = "euler.security.web.logout-processing-url";

    public static final boolean SIGNUP_ENABLED = true;
    public static final String SIGNUP_PAGE = "/signup";
    public static final String LOGIN_PAGE = "/login";
    public static final String LOGOUT_PAGE = "/logout";
    public static final String SIGNUP_PROCESSING_URL = "/signup";
    public static final String LOGIN_PROCESSING_URL = "/login";
    public static final String LOGOUT_PROCESSING_URL = "/logout";
}
