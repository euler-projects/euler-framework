package org.eulerframework.exception;

import org.eulerframework.context.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;

import java.util.Locale;

public class EulerException extends Exception {

    public EulerException() {
        super();
    }

    public EulerException(String message) {
        super(message);
    }

    public EulerException(Throwable cause) {
        super(cause);
    }

    public EulerException(String message, Throwable cause) {
        super(message, cause);
    }

    protected EulerException(String message, Throwable cause, boolean enableSuppression,
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
