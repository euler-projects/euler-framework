package net.eulerframework.web.core.exception.web;

import net.eulerframework.web.core.i18n.Tag;

@SuppressWarnings("serial")
public abstract class WebException extends RuntimeException {

    private String message;
    private int code;

    public WebException(String message, int code) {
        super(message);
        this.message = message;
        this.code = code;
    }

    public WebException(String message, int code, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = code;
    }

    protected WebException(String message, int code, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String getLocalizedMessage() {
        return Tag.i18n(this.getMessage());
    }

    public int getCode() {
        return this.code;
    }

}
