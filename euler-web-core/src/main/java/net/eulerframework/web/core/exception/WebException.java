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
    
    public enum WebError {
        
        ACCESS_DENIED(70301, "access_denied"),
        
        ILLEGAL_ARGUMENT(70401, "illegal_argument"),
        ILLEGAL_PARAM(70402, "illegal_param"),
        
        RESOURCE_NOT_FOUND(70701, "resource_not_found"),
        RESOURCE_EXISTS(70702, "resource_exists"),
        RESOURCE_STATUS_LOCKED(70703, "resource_status_locked"),
        
        UNDEFINED_ERROR(-1, "undefined_error"),
        UNKNOWN_ERROR(-1, "unknown_error");
        
        private final int value;

        private final String reasonPhrase;


        private WebError(int value, String reasonPhrase) {
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
