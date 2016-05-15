package net.eulerform.web.core.base.exception;

public class EntityCompareException extends RuntimeException {

    private static final long serialVersionUID = 531286401255349009L;

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
