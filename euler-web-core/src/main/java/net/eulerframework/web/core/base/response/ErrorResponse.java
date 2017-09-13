package net.eulerframework.web.core.base.response;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.exception.web.WebRuntimeException;
import net.eulerframework.web.core.exception.web.WebError;

public class ErrorResponse extends LogSupport implements BaseResponse {

    private String error;
    private Integer error_code;
    private String error_description;
    
    public ErrorResponse() {
        this.error = WebError.UNDEFINED_ERROR.getReasonPhrase();
        this.error_code = WebError.UNDEFINED_ERROR.value();        
    }

    public ErrorResponse(WebRuntimeException webRuntimeException) {
        if (WebConfig.isDebugMode()) {
            this.logger.error("WebRuntimeException throwed," + " error: " + webRuntimeException.getError() + " code: " + webRuntimeException.getCode() + " message: " + webRuntimeException.getMessage(),
                    webRuntimeException);
        }

        this.error = webRuntimeException.getError();
        this.error_code = webRuntimeException.getCode();
        this.error_description = webRuntimeException.getLocalizedMessage();
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
