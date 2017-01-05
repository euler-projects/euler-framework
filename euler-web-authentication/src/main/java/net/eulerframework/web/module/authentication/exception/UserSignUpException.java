package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class UserSignUpException extends Exception {
    public UserSignUpException() {
        super();
    }

    public UserSignUpException(String message) {
        super(message);
    }

    public UserSignUpException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserSignUpException(Throwable cause) {
        super(cause);
    }
    
    protected UserSignUpException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
