package net.eulerframework.web.module.authentication.exception;

import net.eulerframework.web.core.base.exception.I18NRuntimeException;

@SuppressWarnings("serial")
public class PasswordException extends I18NRuntimeException {
    public PasswordException() {
        super();
    }

    public PasswordException(String message) {
        super(message);
    }

    public PasswordException(String message, Throwable cause) {
        super(message, cause);
    }

    public PasswordException(Throwable cause) {
        super(cause);
    }
    
    protected PasswordException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
