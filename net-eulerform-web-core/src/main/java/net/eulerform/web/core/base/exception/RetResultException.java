package net.eulerform.web.core.base.exception;

public class RetResultException extends RuntimeException {

    private static final long serialVersionUID = 5686669038870484278L;

    public RetResultException() {
        super();
    }

    public RetResultException(String message) {
        super(message);
    }

    public RetResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetResultException(Throwable cause) {
        super(cause);
    }
    
    protected RetResultException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
