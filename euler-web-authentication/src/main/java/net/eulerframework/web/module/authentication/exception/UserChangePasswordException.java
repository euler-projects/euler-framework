package net.eulerframework.web.module.authentication.exception;

import net.eulerframework.web.core.base.exception.I18NException;

@SuppressWarnings("serial")
public class UserChangePasswordException extends I18NException {
    
    public enum INFO {
        UNKNOWN_CHANGE_PASSWD_ERROR;
    }

    public UserChangePasswordException() {
        super();
    }

    public UserChangePasswordException(String message) {
        super(message);
    }

    public UserChangePasswordException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserChangePasswordException(Throwable cause) {
        super(cause);
    }
    
    protected UserChangePasswordException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
