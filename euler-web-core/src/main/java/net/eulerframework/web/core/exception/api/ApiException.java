package net.eulerframework.web.core.exception.api;

import net.eulerframework.web.core.exception.WebException;

@SuppressWarnings("serial")
public abstract class ApiException extends WebException {
    
    private int httpStatus;    

    public ApiException(String error, int code, int httpStatus) {
        super(error, code);
        this.httpStatus = httpStatus;
    }
    
    public ApiException(String message, String error, int code, int httpStatus) {
        super(message, error, code);
        this.httpStatus = httpStatus;
    }

    public ApiException(String error, int code, int httpStatus, Throwable cause) {
        super(error, code, cause);
        this.httpStatus = httpStatus;
    }

    public ApiException(String message, String error, int code, int httpStatus, Throwable cause) {
        super(message, error, code, cause);
        this.httpStatus = httpStatus;
    }

    protected ApiException(String message, String error, int code, int httpStatus, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, error, code, cause, enableSuppression, writableStackTrace);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

}
