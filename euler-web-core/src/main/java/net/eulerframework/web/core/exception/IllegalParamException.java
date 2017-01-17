package net.eulerframework.web.core.exception;



@SuppressWarnings("serial")
public class IllegalParamException extends RuntimeException {
    
    public IllegalParamException() {
        super();
    }

    public IllegalParamException(String message) {
        super(message);
    }

    public IllegalParamException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalParamException(Throwable cause) {
        super(cause);
    }
    
    protected IllegalParamException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
