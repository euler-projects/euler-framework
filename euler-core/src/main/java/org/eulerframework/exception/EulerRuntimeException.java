package org.eulerframework.exception;

import org.eulerframework.context.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;

import java.util.Locale;

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

    @Override
    public String getLocalizedMessage() {
        ApplicationContext applicationContext = ApplicationContextHolder.getApplicationContext();

        if(applicationContext == null) {
            return super.getLocalizedMessage();
        }

        return applicationContext.getMessage(this.getMessage(), null, Locale.SIMPLIFIED_CHINESE);
    }
}
