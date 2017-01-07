package net.eulerframework.web.core.base.exception;

import net.eulerframework.web.core.i18n.Tag;

@SuppressWarnings("serial")
public class I18NRuntimeException extends RuntimeException {
    
    public I18NRuntimeException() {
        super();
    }

    public I18NRuntimeException(String message) {
        super(message);
    }

    public I18NRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public I18NRuntimeException(Throwable cause) {
        super(cause);
    }
    
    protected I18NRuntimeException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public String getLocalizedMessage() {
        return Tag.i18n(this.getMessage());
    }
}
