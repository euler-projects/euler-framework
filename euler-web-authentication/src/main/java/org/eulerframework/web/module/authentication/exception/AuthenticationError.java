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
package org.eulerframework.web.module.authentication.exception;

import org.eulerframework.web.core.exception.web.WebError;

/**
 * 系统预定义用户认证模块异常代码
 * 
 * <p>所有系统预定义用户认证模块异常代范围为{@code 710000 ~ 719999}</p>
 * 
 * @author cFrost
 *
 */
public enum AuthenticationError implements WebError {
    
    PASSWD_RESET_ERROR(710000, "passwd_reset_error"),
    USER_NOT_FOUND(710404, "user_not_found");
    
    private final int value;

    private final String reasonPhrase;


    private AuthenticationError(int value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Return the integer value of this web error code.
     */
    public int value() {
        return this.value;
    }

    /**
     * Return the reason phrase of this web error code.
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }
}