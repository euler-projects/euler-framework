/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2017 cFrost.sun(孙宾, SUN BIN) 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following website
 * 
 * https://eulerproject.io
 * https://github.com/euler-projects/euler-framework
 * https://cfrost.net
 */
package net.eulerframework.web.core.exception.web;

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