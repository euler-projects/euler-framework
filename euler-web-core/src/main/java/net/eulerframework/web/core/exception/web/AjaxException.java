package net.eulerframework.web.core.exception.web;

@SuppressWarnings("serial")
public abstract class AjaxException extends WebException {
    
    public AjaxException(String error, int code) {
        super(error, code);
    }
    
    public AjaxException(String message, String error, int code) {
        super(message, error, code);
    }

    public AjaxException(String error, int code, Throwable cause) {
        super(error, code, cause);
    }

    public AjaxException(String message, String error, int code, Throwable cause) {
        super(message, error, code, cause);
    }

    protected AjaxException(String message, String error, int code, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, error, code, cause, enableSuppression, writableStackTrace);
    }
}
