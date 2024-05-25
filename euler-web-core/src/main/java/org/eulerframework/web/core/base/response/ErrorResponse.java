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

    private String error;
    private Integer error_code;
    private String error_description;
    
    public ErrorResponse() {
        this.error = SystemWebError.UNDEFINED_ERROR.getReasonPhrase();
        this.error_code = SystemWebError.UNDEFINED_ERROR.value();        
    }

    public ErrorResponse(WebException webException) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("WebException throwed," + " error: " + webException.getError() + " code: " + webException.getCode() + " message: " + webException.getMessage(),
                    webException);
        }

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
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        buffer.append('{');
        if(this.error != null) {
            buffer.append("\"error\":");
            buffer.append('\"');
            buffer.append(error);
            buffer.append('\"');
            first = false;
        }
        if(this.error_code != null) {
            if(!first) {
                buffer.append(',');
            }            
            buffer.append("\"error_code\":");
            buffer.append(error_code);
            first = false;
        }
        if(this.error_description != null) {
            if(!first) {
                buffer.append(',');
            }            
            buffer.append("\"error_description\":");
            buffer.append('\"');
            buffer.append(error_description);
            buffer.append('\"');
        }
        buffer.append('}');
        
        return buffer.toString();
    }
}
