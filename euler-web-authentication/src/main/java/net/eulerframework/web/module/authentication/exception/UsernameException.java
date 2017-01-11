package net.eulerframework.web.module.authentication.exception;

import net.eulerframework.web.core.base.exception.I18NRuntimeException;

@SuppressWarnings("serial")
public class UsernameException extends I18NRuntimeException {
    public UsernameException() {
        super();
    }

    public UsernameException(String message) {
        super(message);
    }

    public UsernameException(String message, Throwable cause) {
        super(message, cause);
    }

    public UsernameException(Throwable cause) {
        super(cause);
    }
    
    protected UsernameException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public enum INFO {
        INCORRECT_USERNAME_FORMAT,USERNAME_USED,USERNAME_IS_NULL;
    }

}
