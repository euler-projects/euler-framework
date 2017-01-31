package net.eulerframework.web.core.exception.web;

@SuppressWarnings("serial")
public abstract class AjaxException extends WebException {
    public AjaxException(String message, int code) {
        super(message, code);
    }

    public AjaxException(String message, int code, Throwable cause) {
        super(message, code, cause);
    }

    protected AjaxException(String message, int code, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, code, cause, enableSuppression, writableStackTrace);
    }
}
