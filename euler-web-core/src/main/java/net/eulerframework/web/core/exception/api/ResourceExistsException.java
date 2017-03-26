package net.eulerframework.web.core.exception.api;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class ResourceExistsException extends ApiException {

    public ResourceExistsException() {
        super(Error.RESOURCE_EXISTS.getReasonPhrase(), Error.RESOURCE_EXISTS.value(), HttpStatus.CONFLICT.value());
    }

    public ResourceExistsException(String message) {
        super(message,Error.RESOURCE_EXISTS.getReasonPhrase(), Error.RESOURCE_EXISTS.value(), HttpStatus.CONFLICT.value());
    }

    public ResourceExistsException(String message, Throwable cause) {
        super(message,Error.RESOURCE_EXISTS.getReasonPhrase(), Error.RESOURCE_EXISTS.value(), HttpStatus.CONFLICT.value(), cause);
    }

    public ResourceExistsException(Throwable cause) {
        super(Error.RESOURCE_EXISTS.getReasonPhrase(), Error.RESOURCE_EXISTS.value(), HttpStatus.CONFLICT.value(), cause);
    }
}
