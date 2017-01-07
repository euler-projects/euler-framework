package net.eulerframework.web.core.exception;

import net.eulerframework.web.core.base.exception.I18NRuntimeException;

@SuppressWarnings("serial")
public class WebControllerException extends I18NRuntimeException {

    public WebControllerException() {
        super();
    }

    public WebControllerException(String message) {
        super(message);
    }

    public WebControllerException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebControllerException(Throwable cause) {
        super(cause);
    }
    
    protected WebControllerException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
