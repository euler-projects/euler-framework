package net.eulerframework.web.core.base.response;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.exception.WebException;

public class ErrorResponse extends LogSupport implements BaseResponse {

    private String error;
    private int error_code;
    private String error_description;
    
    public ErrorResponse() {
        
    }
    
    public ErrorResponse(Exception exception) {
        this.error = exception.getMessage();
        this.error_code = -1;
        this.error_description = exception.getLocalizedMessage();
    }

    public ErrorResponse(WebException webException) {
        if (WebConfig.isDebugMode()) {
            this.logger.error("WebException throwed," + " error: " + webException.getError() + " code: " + webException.getCode() + " message: " + webException.getMessage(),
                    webException);
        }

        this.error = webException.getError();
        this.error_code = webException.getCode();
        this.error_description = webException.getLocalizedMessage();
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public String getError_description() {
        return error_description;
    }

    public void setError_description(String error_description) {
        this.error_description = error_description;
    }
}
