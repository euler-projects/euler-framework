package net.eulerframework.web.module.authentication.exception;

public class InvalidEmailResetTokenException extends Exception {

    public InvalidEmailResetTokenException() {
        super();
    }

    public InvalidEmailResetTokenException(String message) {
        super(message);
    }

    public InvalidEmailResetTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidEmailResetTokenException(Throwable cause) {
        super(cause);
    }
    
    protected InvalidEmailResetTokenException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
