package net.eulerframework.web.core.exception;

@SuppressWarnings("serial")
public abstract class ViewException extends RuntimeException {
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

    public abstract String getViewInfo();

}
