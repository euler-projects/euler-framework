package net.eulerframework.web.core.exception;



@SuppressWarnings("serial")
public class EntityCompareException extends RuntimeException {

    public EntityCompareException() {
        super();
    }

    public EntityCompareException(String message) {
        super(message);
    }

    public EntityCompareException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityCompareException(Throwable cause) {
        super(cause);
    }
    
    protected EntityCompareException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
