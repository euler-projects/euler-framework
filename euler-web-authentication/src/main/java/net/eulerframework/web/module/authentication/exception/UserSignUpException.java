package net.eulerframework.web.module.authentication.exception;

import net.eulerframework.web.core.base.exception.I18NRuntimeException;

@SuppressWarnings("serial")
public class UserSignUpException extends I18NRuntimeException {
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
