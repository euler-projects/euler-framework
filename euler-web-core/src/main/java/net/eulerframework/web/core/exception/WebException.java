package net.eulerframework.web.core.exception;

import net.eulerframework.web.core.i18n.Tag;

@SuppressWarnings("serial")
public abstract class WebException extends RuntimeException {
    
    private String error;
    private int code;

    public WebException(String error, int code) {
        super();
        this.error = error;
        this.code = code;
    }

    public WebException(String message, String error, int code) {
        super(message);
        this.error = error;
        this.code = code;
    }

    public WebException(String error, int code, Throwable cause) {
        super(cause);
        this.error = error;
        this.code = code;
    }

    public WebException(String message, String error, int code, Throwable cause) {
        super(message, cause);
        this.error = error;
        this.code = code;
    }

    protected WebException(String message, String error, int code, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.error = error;
        this.code = code;
    }

    @Override
    public String getLocalizedMessage() {
        return Tag.i18n(this.getMessage());
    }

    public int getCode() {
        return this.code;
    }
    
    public String getError() {
        return this.error;
    }

}
