package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class InvalidSMSResetCodeException extends Exception {

    public InvalidSMSResetCodeException() {
        super();
    }

    public InvalidSMSResetCodeException(String message) {
        super(message);
    }

    public InvalidSMSResetCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSMSResetCodeException(Throwable cause) {
        super(cause);
    }
    
    protected InvalidSMSResetCodeException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
