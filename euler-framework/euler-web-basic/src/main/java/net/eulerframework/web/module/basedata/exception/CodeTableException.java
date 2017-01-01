package net.eulerframework.web.module.basedata.exception;

@SuppressWarnings("serial")
public class CodeTableException extends Exception {

    public CodeTableException() {
        super();
    }

    public CodeTableException(String message) {
        super(message);
    }

    public CodeTableException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodeTableException(Throwable cause) {
        super(cause);
    }
    
    protected CodeTableException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
