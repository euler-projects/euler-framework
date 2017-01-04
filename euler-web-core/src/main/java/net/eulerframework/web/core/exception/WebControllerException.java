package net.eulerframework.web.core.exception;

@SuppressWarnings("serial")
public class WebControllerException extends RuntimeException {

    public WebControllerException() {
        super();
    }

    public WebControllerException(String message) {
        super(message);
    }

    public WebControllerException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebControllerException(Throwable cause) {
        super(cause);
    }
    
    protected WebControllerException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
