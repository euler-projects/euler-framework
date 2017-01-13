package net.eulerframework.web.core.exception.view;

import net.eulerframework.web.core.i18n.Tag;

@SuppressWarnings("serial")
public class ViewException extends RuntimeException {
    public ViewException() {
        super();
    }

    public ViewException(String message) {
        super(message);
    }

    public ViewException(String message, Throwable cause) {
        super(message, cause);
    }

    public ViewException(Throwable cause) {
        super(cause);
    }
    
    protected ViewException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public String getLocalizedMessage() {
        return Tag.i18n(this.getMessage());
    }

}
