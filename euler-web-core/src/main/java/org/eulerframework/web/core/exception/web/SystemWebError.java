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
package org.eulerframework.web.core.exception.web;

/**
 * 系统预定义WEB异常代码
 * 
 * <p>所有系统预定义的错误代码范围为{@code 700000 ~ 799999}</p>
 * <pre>
 * 703000 ~ 703999 权限异常
 * 704000 ~ 704999 请求参数异常
 * 707000 ~ 707999 请求资源异常
 * 710000 ~ 719999 身份认证模块异常
 * -1 未定义异常
 * </pre>
 * 
 * @author cFrost
 *
 */
public enum SystemWebError implements WebError {
    
    ACCESS_DENIED(703001, "access_denied"),

    ILLEGAL_ARGUMENT(704001, "illegal_argument"),
    ILLEGAL_PARAMETER(704002, "illegal_parameter"),
    PARAMETER_NOT_MEET_REQUIREMENT(704003, "parameter_not_meet_requirement"),
    
    RESOURCE_NOT_FOUND(707001, "resource_not_found"),
    RESOURCE_EXISTS(707002, "resource_exists"),
    RESOURCE_STATUS_LOCKED(707003, "resource_status_locked"), 

    BAD_CREDENTIALS(710401, "bad_credentials"),
    
    UNDEFINED_ERROR(-1, "undefined_error");
    
    private final int value;

    private final String reasonPhrase;


    private SystemWebError(int value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    @Override
    public int value() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return reasonPhrase;
    }
}