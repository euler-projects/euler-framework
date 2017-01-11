package net.eulerframework.web.module.authentication.exception;

import net.eulerframework.web.core.base.exception.I18NException;

@SuppressWarnings("serial")
public class UserNotFoundException extends I18NException {
    public UserNotFoundException() {
        super();
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Throwable cause) {
        super(cause);
    }
    
    protected UserNotFoundException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
