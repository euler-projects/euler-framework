package net.eulerframework.web.core.exception;



@SuppressWarnings("serial")
public class ResourceExistException extends RuntimeException {

    public ResourceExistException() {
        super();
    }

    public ResourceExistException(String message) {
        super(message);
    }

    public ResourceExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceExistException(Throwable cause) {
        super(cause);
    }
    
    protected ResourceExistException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
