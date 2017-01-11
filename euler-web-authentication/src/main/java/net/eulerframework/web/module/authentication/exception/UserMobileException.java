package net.eulerframework.web.module.authentication.exception;

import net.eulerframework.web.core.base.exception.I18NRuntimeException;

@SuppressWarnings("serial")
public class UserMobileException extends I18NRuntimeException {
    public UserMobileException() {
        super();
    }

    public UserMobileException(String message) {
        super(message);
    }

    public UserMobileException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserMobileException(Throwable cause) {
        super(cause);
    }
    
    protected UserMobileException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public enum INFO {
        INCORRECT_MOBILE_FORMAT,MOBILE_USED,NULL;
    }

}
