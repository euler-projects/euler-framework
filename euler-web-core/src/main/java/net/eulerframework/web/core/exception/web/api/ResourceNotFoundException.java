package net.eulerframework.web.core.exception.web.api;

import net.eulerframework.web.core.exception.web.SystemWebError;
import net.eulerframework.web.core.exception.web.WebRuntimeException;

public class ResourceNotFoundException extends WebRuntimeException {

    public ResourceNotFoundException() {
        super(SystemWebError.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
        super(message, SystemWebError.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, SystemWebError.RESOURCE_NOT_FOUND, cause);
    }

    public ResourceNotFoundException(Throwable cause) {
        super(SystemWebError.RESOURCE_NOT_FOUND, cause);
    }
    
    protected ResourceNotFoundException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, SystemWebError.RESOURCE_NOT_FOUND, cause, enableSuppression, writableStackTrace);
    }
}
