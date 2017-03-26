package net.eulerframework.web.core.exception;

import net.eulerframework.web.core.i18n.Tag;

@SuppressWarnings("serial")
public abstract class WebException extends RuntimeException {
    
    private String error;
    private int code;

    public WebException(String error, int code) {
        super();
        this.error = error;
        this.code = code;
    }

    public WebException(String message, String error, int code) {
        super(message);
        this.error = error;
        this.code = code;
    }

    public WebException(String error, int code, Throwable cause) {
        super(cause);
        this.error = error;
        this.code = code;
    }

    public WebException(String message, String error, int code, Throwable cause) {
        super(message, cause);
        this.error = error;
        this.code = code;
    }

    protected WebException(String message, String error, int code, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.error = error;
        this.code = code;
    }

    @Override
    public String getLocalizedMessage() {
        return Tag.i18n(this.getMessage());
    }

    public int getCode() {
        return this.code;
    }
    
    public String getError() {
        return this.error;
    }
    
    protected enum Error {
        RESOURCE_STATUS_LOCKED(7431, "resource_status_locked"),
        RESOURCE_EXISTS(409, "resource_exists"),
        UNDEFINED_ERROR(-1, "undefined_error"),
        UNKNOWN_ERROR(-1, "unknown_error");
        
        private final int value;

        private final String reasonPhrase;


        private Error(int value, String reasonPhrase) {
            this.value = value;
            this.reasonPhrase = reasonPhrase;
        }

        /**
         * Return the integer value of this status code.
         */
        public int value() {
            return this.value;
        }

        /**
         * Return the reason phrase of this status code.
         */
        public String getReasonPhrase() {
            return reasonPhrase;
        }
    }

}
