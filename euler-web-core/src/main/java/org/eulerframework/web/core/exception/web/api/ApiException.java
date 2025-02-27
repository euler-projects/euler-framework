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
package org.eulerframework.web.core.exception.web.api;

import org.eulerframework.web.core.exception.web.WebError;
import org.eulerframework.web.core.exception.web.WebException;
import org.springframework.http.HttpStatus;

public abstract class ApiException extends WebException {
    public ApiException(WebError webError, HttpStatus httpStatus) {
        super(webError, httpStatus);
    }
    
    public ApiException(String message, WebError webError, HttpStatus httpStatus) {
        super(message, webError, httpStatus);
    }

    public ApiException(WebError webError, Throwable cause, HttpStatus httpStatus) {
        super(webError, cause, httpStatus);
    }

    public ApiException(String message, WebError webError, Throwable cause, HttpStatus httpStatus) {
        super(message, webError, cause, httpStatus);
    }

    protected ApiException(String message, WebError webError, Throwable cause, boolean enableSuppression, boolean writableStackTrace, HttpStatus httpStatus) {
        super(message, webError, cause, enableSuppression, writableStackTrace, httpStatus);
    }
}
