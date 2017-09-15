package net.eulerframework.web.core.exception.web.api;

import net.eulerframework.web.core.exception.web.WebError;
import net.eulerframework.web.core.exception.web.WebRuntimeException;

public abstract class ApiException extends WebRuntimeException {
    
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
