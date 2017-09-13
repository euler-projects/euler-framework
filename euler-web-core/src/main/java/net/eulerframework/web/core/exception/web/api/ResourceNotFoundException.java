package net.eulerframework.web.core.exception.web.api;

import net.eulerframework.web.core.exception.web.WebError;
import net.eulerframework.web.core.exception.web.WebRuntimeException;

public class ResourceNotFoundException extends WebRuntimeException {

    public ResourceNotFoundException() {
        super(WebError.RESOURCE_NOT_FOUND.getReasonPhrase(), WebError.RESOURCE_NOT_FOUND.value());
    }

    public ResourceNotFoundException(String message) {
        super(message, WebError.RESOURCE_NOT_FOUND.getReasonPhrase(), WebError.RESOURCE_NOT_FOUND.value());
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, WebError.RESOURCE_NOT_FOUND.getReasonPhrase(), WebError.RESOURCE_NOT_FOUND.value(), cause);
    }

    public ResourceNotFoundException(Throwable cause) {
        super(WebError.RESOURCE_NOT_FOUND.getReasonPhrase(), WebError.RESOURCE_NOT_FOUND.value(), cause);
    }
    
    protected ResourceNotFoundException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, WebError.RESOURCE_NOT_FOUND.getReasonPhrase(), WebError.RESOURCE_NOT_FOUND.value(), cause, enableSuppression, writableStackTrace);
    }
}
