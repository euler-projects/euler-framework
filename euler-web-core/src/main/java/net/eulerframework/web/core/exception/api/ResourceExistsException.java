package net.eulerframework.web.core.exception.api;

@SuppressWarnings("serial")
public class ResourceExistsException extends ApiException {

    public ResourceExistsException() {
        super("resource_exists", 100);
    }

    public ResourceExistsException(String message) {
        super(message,"resource_exists", 100);
    }

    public ResourceExistsException(String message, Throwable cause) {
        super(message,"resource_exists", 100, cause);
    }

    public ResourceExistsException(Throwable cause) {
        super("resource_exists", 100, cause);
    }
}
