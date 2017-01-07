package net.eulerframework.web.core.exception;

import net.eulerframework.web.core.base.exception.I18NRuntimeException;

@SuppressWarnings("serial")
public class EntityCompareException extends I18NRuntimeException {

    public EntityCompareException() {
        super();
    }

    public EntityCompareException(String message) {
        super(message);
    }

    public EntityCompareException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityCompareException(Throwable cause) {
        super(cause);
    }
    
    protected EntityCompareException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
