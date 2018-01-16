package net.eulerframework.web.core.exception.web;

import net.eulerframework.common.util.StringUtils;
import net.eulerframework.web.core.i18n.Tag;

public class WebRuntimeException extends RuntimeException {
    
    private String error;
    private int code;

    public WebRuntimeException() {
        super();
        this.generateErrorAndCode();
    }
    
    public WebRuntimeException(String message) {
        super(message);
        this.generateErrorAndCode();
    }

    public WebRuntimeException(Throwable cause) {
        super(cause);
        this.generateErrorAndCode();
    }

    public WebRuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.generateErrorAndCode();
    }

    protected WebRuntimeException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.generateErrorAndCode();
    }
    
    public WebRuntimeException(WebError webError) {
        this.generateErrorAndCode(webError);
    }

    public WebRuntimeException(String message, WebError webError) {
        super(message);
        this.generateErrorAndCode(webError);
    }

    public WebRuntimeException(WebError webError, Throwable cause) {
        super(cause);
        this.generateErrorAndCode(webError);
    }

    public WebRuntimeException(String message, WebError webError, Throwable cause) {
        super(message, cause);
        this.generateErrorAndCode(webError);
    }

    protected WebRuntimeException(String message, WebError webError, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.generateErrorAndCode(webError);
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
    
    private void generateErrorAndCode() {
        this.error = this.getClass().getSimpleName();
        if(this.error.endsWith("Exception")) {
            this.error.substring(0, this.error.length() - "Exception".length());
        }
        this.error = StringUtils.camelCaseToUnderLineCase(this.error);
        this.code = this.error.hashCode();
    }
    
    private void generateErrorAndCode(WebError webError) {
        this.error = webError.getReasonPhrase();
        this.code = webError.value();
    }
}
