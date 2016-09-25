package net.eulerform.web.core.exception;

@SuppressWarnings("serial")
public class MultipartFileSaveException extends Exception {

    public MultipartFileSaveException() {
        super();
    }

    public MultipartFileSaveException(String message) {
        super(message);
    }

    public MultipartFileSaveException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipartFileSaveException(Throwable cause) {
        super(cause);
    }
    
    protected MultipartFileSaveException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
