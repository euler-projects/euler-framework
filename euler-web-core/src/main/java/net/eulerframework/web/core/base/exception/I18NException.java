package net.eulerframework.web.core.base.exception;

import net.eulerframework.web.core.i18n.Tag;

@SuppressWarnings("serial")
public class I18NException extends Exception {
    public I18NException() {
        super();
    }

    public I18NException(String message) {
        super(message);
    }

    public I18NException(String message, Throwable cause) {
        super(message, cause);
    }

    public I18NException(Throwable cause) {
        super(cause);
    }
    
    protected I18NException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public String getLocalizedMessage() {
        return Tag.i18n(this.getMessage());
    }
}
