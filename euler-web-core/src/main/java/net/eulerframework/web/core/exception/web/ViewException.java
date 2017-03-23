package net.eulerframework.web.core.exception.web;

import net.eulerframework.web.core.exception.WebException;

@SuppressWarnings("serial")
public abstract class ViewException extends WebException {
    
    public ViewException(String error, int code) {
        super(error, code);
    }
    
    public ViewException(String message, String error, int code) {
        super(message, error, code);
    }

    public ViewException(String error, int code, Throwable cause) {
        super(error, code, cause);
    }

    public ViewException(String message, String error, int code, Throwable cause) {
        super(message, error, code, cause);
    }

    protected ViewException(String message, String error, int code, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, error, code, cause, enableSuppression, writableStackTrace);
    }
}
