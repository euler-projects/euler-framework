/*
 * Copyright 2013-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.security.web.endpoint;

public abstract class EulerSecurityEndpoints {
    public static final String PROPERTY_NAME_PREFIX = "euler.security.web.endpoint";
    public static final String USER_ENDPOINT_PROP_PREFIX = PROPERTY_NAME_PREFIX + "." + "user";
    public static final String PASSWORD_ENDPOINT_PROP_PREFIX = PROPERTY_NAME_PREFIX + "." + "password";
    public static final String SIGNUP_ENDPOINT_PROP_PREFIX = PROPERTY_NAME_PREFIX + "." + "signup";
    public static final String CSRF_ENDPOINT_PROP_PREFIX = PROPERTY_NAME_PREFIX + "." + "csrf";
    public static final String ENABLED_PROP = "enabled";

    public static final String USER_ENABLED_PROP_NAME = USER_ENDPOINT_PROP_PREFIX + "." + ENABLED_PROP;
    public static final String USER_LOGIN_PAGE_PROP_NAME = USER_ENDPOINT_PROP_PREFIX + "." + "login-page";
    public static final String USER_LOGOUT_PAGE_PROP_NAME = USER_ENDPOINT_PROP_PREFIX + "." + "logout-page";
    public static final String USER_LOGIN_PROCESSING_URL_PROP_NAME = USER_ENDPOINT_PROP_PREFIX + "." + "login-processing-url";
    public static final String USER_LOGOUT_PROCESSING_URL_PROP_NAME = USER_ENDPOINT_PROP_PREFIX + "." + "logout-processing-url";
    public static final String USER_LOGIN_SUCCESS_REDIRECT_PARAMETER_PROP_NAME = USER_ENDPOINT_PROP_PREFIX + "." + "login-success-redirect-parameter";

    public static final String SIGNUP_ENABLED_PROP_NAME = SIGNUP_ENDPOINT_PROP_PREFIX + "." + ENABLED_PROP;
    public static final String SIGNUP_PAGE_PROP_NAME = SIGNUP_ENDPOINT_PROP_PREFIX + "." + "signup-page";
    public static final String SIGNUP_PROCESSING_URL_PROP_NAME = SIGNUP_ENDPOINT_PROP_PREFIX + "." + "signup-processing-url";

    public static final String PASSWORD_ENABLED_PROP_NAME = PASSWORD_ENDPOINT_PROP_PREFIX + "." + ENABLED_PROP;
    public static final String PASSWORD_CHANGE_PASSWORD_PAGE_PROP_NAME = PASSWORD_ENDPOINT_PROP_PREFIX + "." + "change-password-page";
    public static final String PASSWORD_CHANGE_PASSWORD_PROCESSING_URL_PROP_NAME = PASSWORD_ENDPOINT_PROP_PREFIX + "." + "change-password-processing-url";

    public static final String CSRF_ENABLED_PROP_NAME = CSRF_ENDPOINT_PROP_PREFIX + "." + ENABLED_PROP;
    public static final String CSRF_PATH_PROP_NAME = CSRF_ENDPOINT_PROP_PREFIX + "." + "path";

    public static final boolean USER_ENABLED = true;
    public static final String USER_LOGIN_PAGE = "/login";
    public static final String USER_LOGOUT_PAGE = "/logout";
    public static final String USER_LOGIN_PROCESSING_URL = "/login";
    public static final String USER_LOGOUT_PROCESSING_URL = "/logout";
    public static final String USER_LOGIN_SUCCESS_REDIRECT_PARAMETER = "continue";

    public static final boolean SIGNUP_ENABLED = false;
    public static final String SIGNUP_PAGE = "/signup";
    public static final String SIGNUP_PROCESSING_URL = "/signup";

    public static final boolean PASSWORD_ENABLED = true;
    public static final String PASSWORD_CHANGE_PASSWORD_PAGE = "/change-password";
    public static final String PASSWORD_CHANGE_PASSWORD_PROCESSING_URL = "/change-password";

    public static final boolean CSRF_ENABLED = false;
    public static final String CSRF_PATH = "/_csrf";
}
