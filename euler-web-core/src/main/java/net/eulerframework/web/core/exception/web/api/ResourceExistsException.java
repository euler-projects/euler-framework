package net.eulerframework.web.core.exception.web.api;

import org.springframework.http.HttpStatus;

import net.eulerframework.web.core.exception.web.SystemWebError;

public class ResourceExistsException extends ApiException {

    public ResourceExistsException() {
        super(SystemWebError.RESOURCE_EXISTS, HttpStatus.CONFLICT.value());
    }

    public ResourceExistsException(String message) {
        super(message, SystemWebError.RESOURCE_EXISTS, HttpStatus.CONFLICT.value());
    }

    public ResourceExistsException(String message, Throwable cause) {
        super(message, SystemWebError.RESOURCE_EXISTS, HttpStatus.CONFLICT.value(), cause);
    }

    public ResourceExistsException(Throwable cause) {
        super(SystemWebError.RESOURCE_EXISTS, HttpStatus.CONFLICT.value(), cause);
    }
}
