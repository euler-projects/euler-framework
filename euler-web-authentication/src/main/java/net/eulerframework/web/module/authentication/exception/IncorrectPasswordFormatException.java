package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class IncorrectPasswordFormatException extends Exception {
    public IncorrectPasswordFormatException() {
        super();
    }

    public IncorrectPasswordFormatException(String message) {
        super(message);
    }

    public IncorrectPasswordFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectPasswordFormatException(Throwable cause) {
        super(cause);
    }
    
    protected IncorrectPasswordFormatException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
