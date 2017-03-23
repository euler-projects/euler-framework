package net.eulerframework.web.core.exception.api;

import org.springframework.http.HttpStatus;

import net.eulerframework.web.core.exception.WebException;

@SuppressWarnings("serial")
public abstract class ApiException extends WebException {
    
    public HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    
    public ApiException(String error, int code) {
        super(error, code);
    }
    
    public ApiException(String message, String error, int code) {
        super(message, error, code);
    }

    public ApiException(String error, int code, Throwable cause) {
        super(error, code, cause);
    }

    public ApiException(String message, String error, int code, Throwable cause) {
        super(message, error, code, cause);
    }

    protected ApiException(String message, String error, int code, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, error, code, cause, enableSuppression, writableStackTrace);
    }
    
    public ApiException(String message, String error, int code, HttpStatus httpStatus) {
        super(message, error, code);
        this.httpStatus = httpStatus;
    }

    public ApiException(String message, String error, int code, HttpStatus httpStatus, Throwable cause) {
        super(message, error, code, cause);
        this.httpStatus = httpStatus;
    }

    protected ApiException(String message, String error, int code, HttpStatus httpStatus, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, error, code, cause, enableSuppression, writableStackTrace);
        this.httpStatus = httpStatus;
    }

}
