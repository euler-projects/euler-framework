package net.eulerframework.web.core.exception.web.api;

import org.springframework.http.HttpStatus;

import net.eulerframework.web.core.exception.web.SystemWebError;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException() {
        super(SystemWebError.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }

    public ResourceNotFoundException(String message) {
        super(message, SystemWebError.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, SystemWebError.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value(), cause);
    }

    public ResourceNotFoundException(Throwable cause) {
        super(SystemWebError.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value(), cause);
    }
    
    protected ResourceNotFoundException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, SystemWebError.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND.value(), 
                cause, enableSuppression, writableStackTrace);
    }
}
