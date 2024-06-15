package org.eulerframework.exception;

public class EulerRuntimeException extends RuntimeException {

    public EulerRuntimeException() {
        super();
    }

    public EulerRuntimeException(String message) {
        super(message);
    }

    public EulerRuntimeException(Throwable cause) {
        super(cause);
    }

    public EulerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    protected EulerRuntimeException(String message, Throwable cause, boolean enableSuppression,
                                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
