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
package net.eulerframework.web.core.exception.web.api;

import net.eulerframework.web.core.exception.web.WebError;
import net.eulerframework.web.core.exception.web.WebException;

public abstract class ApiException extends WebException {
    
    private int httpStatus;    

    public ApiException(WebError webError, int httpStatus) {
        super(webError);
        this.httpStatus = httpStatus;
    }
    
    public ApiException(String message, WebError webError, int httpStatus) {
        super(message, webError);
        this.httpStatus = httpStatus;
    }

    public ApiException(WebError webError, int httpStatus, Throwable cause) {
        super(webError, cause);
        this.httpStatus = httpStatus;
    }

    public ApiException(String message, WebError webError, int httpStatus, Throwable cause) {
        super(message, webError, cause);
        this.httpStatus = httpStatus;
    }

    protected ApiException(String message, WebError webError, int httpStatus, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, webError, cause, enableSuppression, writableStackTrace);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

}
