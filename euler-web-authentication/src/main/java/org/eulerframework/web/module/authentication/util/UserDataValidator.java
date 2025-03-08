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
package org.eulerframework.web.module.authentication.util;

import org.springframework.util.StringUtils;

import org.eulerframework.web.module.authentication.conf.SecurityConfig;
import org.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;
import org.eulerframework.web.module.authentication.exception.UserNotFoundException;
import org.eulerframework.web.module.authentication.service.EulerUserEntityService;

/**
 * @author cFrost
 *
 */
public abstract class UserDataValidator {

    private static EulerUserEntityService eulerUserEntityService;
    
    private final static String _USERNAME_IS_NULL = "_USERNAME_IS_NULL";
    private final static String _INCORRECT_USERNAME_FORMAT = "_INCORRECT_USERNAME_FORMAT";
    private final static String _USERNAME_ALREADY_BE_USED = "_USERNAME_ALREADY_BE_USED";
    
    private final static String _EMAIL_IS_NULL = "_EMAIL_IS_NULL";
    private final static String _INCORRECT_EMAIL_FORMAT = "_INCORRECT_EMAIL_FORMAT";
    private final static String _EMAIL_ALREADY_BE_USED = "_EMAIL_ALREADY_BE_USED";
    
    private final static String _PHONE_IS_NULL = "_PHONE_IS_NULL";
    private final static String _INCORRECT_PHONE_FORMAT = "_INCORRECT_PHONE_FORMAT";
    private final static String _PHONE_ALREADY_BE_USED = "_PHONE_ALREADY_BE_USED";
    
    private final static String _PASSWORD_IS_NULL = "_PASSWORD_IS_NULL";
    private final static String _INCORRECT_PASSWORD_FORMAT = "_INCORRECT_PASSWORD_FORMAT";
    private final static String _INCORRECT_PASSWORD_LENGTH = "_INCORRECT_PASSWORD_LENGTH";
    
    public static void setEulerUserEntityService(EulerUserEntityService eulerUserEntityService) {
        UserDataValidator.eulerUserEntityService = eulerUserEntityService;
    }
    
    public static void validUsername(String username) throws UserInfoCheckWebException {
        if (!StringUtils.hasText(username)) {
            throw new UserInfoCheckWebException(_USERNAME_IS_NULL);
        }
        if (!(username.matches(SecurityConfig.getUsernameFormat()))) {
            throw new UserInfoCheckWebException(_INCORRECT_USERNAME_FORMAT);
        }
        
        try {
            eulerUserEntityService.loadUserByUsername(username);
            /*
             * Program running here means this username has already been used!
             */
            throw new UserInfoCheckWebException(_USERNAME_ALREADY_BE_USED);
        } catch (UserNotFoundException e) {
            /*
             * User not found means this username has not been used!
             */
        }
    }

    public static void validEmail(String email) throws UserInfoCheckWebException {
        if (!StringUtils.hasText(email)) {
            throw new UserInfoCheckWebException(_EMAIL_IS_NULL);
        }
        if (!(email.matches(SecurityConfig.getEmailFormat()))) {
            throw new UserInfoCheckWebException(_INCORRECT_EMAIL_FORMAT);
        }
        try {
            eulerUserEntityService.loadUserByEmail(email);
            /*
             * Program running here means this email has already been used!
             */
            throw new UserInfoCheckWebException(_EMAIL_ALREADY_BE_USED);
        } catch (UserNotFoundException e) {
            /*
             * User not found means this email has not been used!
             */
        }
    }

    public static void validPhone(String phone) throws UserInfoCheckWebException {
        if (!StringUtils.hasText(phone)) {
            throw new UserInfoCheckWebException(_PHONE_IS_NULL);
        }
        if (!(phone.matches(SecurityConfig.getPhoneFormat()))) {
            throw new UserInfoCheckWebException(_INCORRECT_PHONE_FORMAT);
        }
        try {
            eulerUserEntityService.loadUserByPhone(phone);
            /*
             * Program running here means this phone has already been used!
             */
            throw new UserInfoCheckWebException(_PHONE_ALREADY_BE_USED);
        } catch (UserNotFoundException e) {
            /*
             * User not found means this phone has not been used!
             */
        }
    }

    public static void validPassword(String password) throws UserInfoCheckWebException {
        if (!StringUtils.hasText(password)) {
            throw new UserInfoCheckWebException(_PASSWORD_IS_NULL);
        }
        if (!password.matches(SecurityConfig.getPasswordFormat())) {
            throw new UserInfoCheckWebException(_INCORRECT_PASSWORD_FORMAT);
        }
        if (!(password.length() >= SecurityConfig.getMinPasswordLength()
                && password.length() <= SecurityConfig.getMaxPasswordLength())) {
            throw new UserInfoCheckWebException(_INCORRECT_PASSWORD_LENGTH);
        }
    }

}
