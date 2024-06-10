/*
 * Copyright 2013-2024 the original author or authors.
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
    public static final String SIGNUP_ENABLED_PROPERTY_NAME = "euler.security.web.signup.enabled";
    public static final String SIGNUP_PAGE_PROPERTY_NAME = "euler.security.web.signup-page";
    public static final String LOGIN_PAGE_PROPERTY_NAME = "euler.security.web.login-page";
    public static final String LOGOUT_PAGE_PROPERTY_NAME = "euler.security.web.logout-page";
    public static final String CHANGE_PASSWORD_PAGE_PROPERTY_NAME = "euler.security.web.change-password-page";
    public static final String SIGNUP_PROCESSING_URL_PROPERTY_NAME = "euler.security.web.signup-processing-url";
    public static final String LOGIN_PROCESSING_URL_PROPERTY_NAME = "euler.security.web.login-processing-url";
    public static final String LOGOUT_PROCESSING_URL_PROPERTY_NAME = "euler.security.web.logout-processing-url";
    public static final String CHANGE_PASSWORD_PROCESSING_URL_PROPERTY_NAME = "euler.security.web.change-password-processing-url";

    public static final boolean SIGNUP_ENABLED = true;
    public static final String SIGNUP_PAGE = "/signup";
    public static final String LOGIN_PAGE = "/login";
    public static final String LOGOUT_PAGE = "/logout";
    public static final String CHANGE_PASSWORD_PAGE = "/change-password";
    public static final String SIGNUP_PROCESSING_URL = "/signup";
    public static final String LOGIN_PROCESSING_URL = "/login";
    public static final String LOGOUT_PROCESSING_URL = "/logout";
    public static final String CHANGE_PASSWORD_PROCESSING_URL = "/change-password";
}
