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

public class ErrorResponse extends LogSupport implements BaseResponse {

    private final String error;
    private final Integer error_code;
    private final String error_description;

    public ErrorResponse() {
        this.error = SystemWebError.UNDEFINED_ERROR.getReasonPhrase();
        this.error_code = SystemWebError.UNDEFINED_ERROR.value();
        this.error_description = null;
    }

    public ErrorResponse(WebException webException) {
        this.logger.debug("WebException thrown, error: {} code: {} message: {}",
                webException.getError(), webException.getCode(), webException.getMessage(), webException);

        this.error = webException.getError();
        this.error_code = webException.getCode();
        this.error_description = webException.getLocalizedMessage();
    }

    public String getError() {
        return error;
    }

    public Integer getError_code() {
        return error_code;
    }

    public String getError_description() {
        return error_description;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        builder.append('{');
        if (this.error != null) {
            builder.append("\"error\":");
            builder.append('\"');
            builder.append(error);
            builder.append('\"');
            first = false;
        }
        if (this.error_code != null) {
            if (!first) {
                builder.append(',');
            }
            builder.append("\"error_code\":");
            builder.append(error_code);
            first = false;
        }
        if (this.error_description != null) {
            if (!first) {
                builder.append(',');
            }
            builder.append("\"error_description\":");
            builder.append('\"');
            builder.append(error_description);
            builder.append('\"');
        }
        builder.append('}');

        return builder.toString();
    }
}
