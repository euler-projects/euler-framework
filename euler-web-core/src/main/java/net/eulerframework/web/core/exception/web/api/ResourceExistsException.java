package net.eulerframework.web.core.exception.web.api;

import org.springframework.http.HttpStatus;

import net.eulerframework.web.core.exception.web.WebError;

public class ResourceExistsException extends ApiException {

    public ResourceExistsException() {
        super(WebError.RESOURCE_EXISTS.getReasonPhrase(), WebError.RESOURCE_EXISTS.value(), HttpStatus.CONFLICT.value());
    }

    public ResourceExistsException(String message) {
        super(message,WebError.RESOURCE_EXISTS.getReasonPhrase(), WebError.RESOURCE_EXISTS.value(), HttpStatus.CONFLICT.value());
    }

    public ResourceExistsException(String message, Throwable cause) {
        super(message,WebError.RESOURCE_EXISTS.getReasonPhrase(), WebError.RESOURCE_EXISTS.value(), HttpStatus.CONFLICT.value(), cause);
    }

    public ResourceExistsException(Throwable cause) {
        super(WebError.RESOURCE_EXISTS.getReasonPhrase(), WebError.RESOURCE_EXISTS.value(), HttpStatus.CONFLICT.value(), cause);
    }
}
