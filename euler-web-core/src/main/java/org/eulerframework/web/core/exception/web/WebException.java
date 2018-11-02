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

import org.eulerframework.common.util.StringUtils;
import org.eulerframework.web.core.i18n.Tag;

public class WebException extends RuntimeException {
    
    private String error;
    private int code;

    public WebException() {
        super();
        this.generateErrorAndCode();
    }
    
    public WebException(String message) {
        super(message);
        this.generateErrorAndCode();
    }

    public WebException(Throwable cause) {
        super(cause);
        this.generateErrorAndCode();
    }

    public WebException(String message, Throwable cause) {
        super(message, cause);
        this.generateErrorAndCode();
    }

    protected WebException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.generateErrorAndCode();
    }
    
    public WebException(WebError webError) {
        this.generateErrorAndCode(webError);
    }

    public WebException(String message, WebError webError) {
        super(message);
        this.generateErrorAndCode(webError);
    }

    public WebException(WebError webError, Throwable cause) {
        super(cause);
        this.generateErrorAndCode(webError);
    }

    public WebException(String message, WebError webError, Throwable cause) {
        super(message, cause);
        this.generateErrorAndCode(webError);
    }

    protected WebException(String message, WebError webError, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.generateErrorAndCode(webError);
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
    
    private void generateErrorAndCode() {
        this.error = this.getClass().getSimpleName();
        if(this.error.endsWith("RuntimeException")) {
            this.error = this.error.substring(0, this.error.length() - "RuntimeException".length());
        } else if(this.error.endsWith("Exception")) {
            this.error = this.error.substring(0, this.error.length() - "Exception".length());
        }
        this.error = StringUtils.camelCaseToUnderLineCase(this.error);
        this.code = this.error.hashCode();
    }
    
    private void generateErrorAndCode(WebError webError) {
        this.error = webError.getReasonPhrase();
        this.code = webError.value();
    }
}
