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
package org.eulerframework.web.core.exception.web;

import org.eulerframework.common.util.StringUtils;
import org.eulerframework.exception.EulerRuntimeException;
import org.eulerframework.web.core.i18n.Tag;
import org.springframework.http.HttpStatus;

public class WebException extends EulerRuntimeException {

    private final String error;
    private final int code;
    private final HttpStatus status;

    public WebException() {
        this((HttpStatus) null);
    }

    public WebException(HttpStatus status) {
        super();
        this.error = this.generateError();
        this.code = this.error.hashCode();
        this.status = status;
    }

    public WebException(String message) {
        this(message, (HttpStatus) null);
    }

    public WebException(String message, HttpStatus status) {
        super(message);
        this.error = this.generateError();
        this.code = this.error.hashCode();
        this.status = status;
    }

    public WebException(Throwable cause) {
        this(cause, (HttpStatus) null);
    }

    public WebException(Throwable cause, HttpStatus status) {
        super(cause);
        this.error = this.generateError();
        this.code = this.error.hashCode();
        this.status = status;
    }

    public WebException(String message, Throwable cause) {
        this(message, cause, (HttpStatus) null);
    }

    public WebException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.error = this.generateError();
        this.code = this.error.hashCode();
        this.status = status;
    }

    protected WebException(String message, Throwable cause, boolean enableSuppression,
                           boolean writableStackTrace) {
        this(message, cause, enableSuppression, writableStackTrace, (HttpStatus) null);
    }

    protected WebException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, HttpStatus status) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.error = this.generateError();
        this.code = this.error.hashCode();
        this.status = status;
    }

    public WebException(WebError webError) {
        this(webError, (HttpStatus) null);
    }

    public WebException(WebError webError, HttpStatus status) {
        this.error = webError.getReasonPhrase();
        this.code = webError.value();
        this.status = status;
    }

    public WebException(String message, WebError webError) {
        this(message, webError, (HttpStatus) null);
    }

    public WebException(String message, WebError webError, HttpStatus status) {
        super(message);
        this.error = webError.getReasonPhrase();
        this.code = webError.value();
        this.status = status;
    }

    public WebException(WebError webError, Throwable cause) {
        this(webError, cause, (HttpStatus) null);
    }

    public WebException(WebError webError, Throwable cause, HttpStatus status) {
        super(cause);
        this.error = webError.getReasonPhrase();
        this.code = webError.value();
        this.status = status;
    }

    public WebException(String message, WebError webError, Throwable cause) {
        this(message, webError, cause, (HttpStatus) null);
    }

    public WebException(String message, WebError webError, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.error = webError.getReasonPhrase();
        this.code = webError.value();
        this.status = status;
    }

    protected WebException(String message, WebError webError, Throwable cause, boolean enableSuppression,
                           boolean writableStackTrace) {
        this(message, webError, cause, enableSuppression, writableStackTrace, (HttpStatus) null);
    }

    protected WebException(String message, WebError webError, Throwable cause, boolean enableSuppression, boolean writableStackTrace, HttpStatus status) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.error = webError.getReasonPhrase();
        this.code = webError.value();
        this.status = status;
    }

    @Override
    public String getLocalizedMessage() {
        return Tag.i18n(this.getMessage());
    }

    public int getCode() {
        return this.code;
    }

    public String getError() {
        return this.error;
    }

    public HttpStatus getStatus() {
        return status;
    }

    private String generateError() {
        String error;
        error = this.getClass().getSimpleName();
        if (error.endsWith("RuntimeException")) {
            error = error.substring(0, error.length() - "RuntimeException".length());
        } else if (error.endsWith("Exception")) {
            error = error.substring(0, error.length() - "Exception".length());
        }
        return StringUtils.camelStyleToUnderLineLowerCase(error);
    }
}
