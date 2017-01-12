package net.eulerframework.web.module.authentication.exception;

import net.eulerframework.web.core.base.exception.I18NRuntimeException;

@SuppressWarnings("serial")
public class UserEmailException extends I18NRuntimeException {
    public UserEmailException() {
        super();
    }

    public UserEmailException(String message) {
        super(message);
    }

    public UserEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserEmailException(Throwable cause) {
        super(cause);
    }
    
    protected UserEmailException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
