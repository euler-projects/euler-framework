package net.eulerform.web.core.base.exception;

@SuppressWarnings("serial")
public class WebInitException extends RuntimeException {

    public WebInitException() {
        super();
    }

    public WebInitException(String message) {
        super(message);
    }

    public WebInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebInitException(Throwable cause) {
        super(cause);
    }
    
    protected WebInitException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
