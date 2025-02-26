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
package org.eulerframework.web.core.base.response;

import org.eulerframework.common.base.log.LogSupport;
import org.eulerframework.web.core.exception.web.SystemWebError;
import org.eulerframework.web.core.exception.web.WebException;
import org.springframework.util.Assert;

import java.util.Date;

public class ErrorResponse extends LogSupport implements BaseResponse {

    private final Date timestamp;
    private final String error;
    private final Integer code;
    private final String message;
    private final String exception;
    private final String trace;

    public ErrorResponse() {
        this(new Date(), SystemWebError.UNDEFINED_ERROR.getReasonPhrase(), SystemWebError.UNDEFINED_ERROR.value(), null, null, null);
    }

    public ErrorResponse(WebException webException) {
        this(new Date(), webException.getError(), webException.getCode(), webException.getLocalizedMessage(), null, null);
    }

    public ErrorResponse(Date timestamp, String error, Integer code, String message, String exception, String trace) {
        Assert.notNull(timestamp, "timestamp must not be null");
        Assert.hasText(error, "error must not be null");
        Assert.notNull(code, "code must not be null");
        this.timestamp = timestamp;
        this.error = error;
        this.code = code;
        this.message = message;
        this.exception = exception;
        this.trace = trace;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getError() {
        return error;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getException() {
        return exception;
    }

    public String getTrace() {
        return trace;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "timestamp=" + timestamp +
                ", error='" + error + '\'' +
                ", code=" + code +
                '}';
    }
}
