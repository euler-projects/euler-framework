package net.eulerframework.web.core.exception.web;

import net.eulerframework.web.core.i18n.Tag;

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
    
    /**
     * 系统预定义WEB异常代码
     * 
     * <p>所有系统预定义的错误代码范围为{@code 700000 ~ 799999}</p>
     * <pre>
     * 703000 ~ 703999 权限异常
     * 704000 ~ 704999 请求参数异常
     * 707000 ~ 707999 请求资源异常
     * -1 未定义异常
     * </pre>
     * 
     * @author cFrost
     *
     */
    public enum WebError {
        
        ACCESS_DENIED(703001, "access_denied"),

        ILLEGAL_ARGUMENT(704001, "illegal_argument"),
        ILLEGAL_PARAMETER(704002, "illegal_parameter"),
        PARAMETER_NOT_MEET_REQUIREMENT(704003, "parameter_not_meet_requirement"),
        
        RESOURCE_NOT_FOUND(707001, "resource_not_found"),
        RESOURCE_EXISTS(707002, "resource_exists"),
        RESOURCE_STATUS_LOCKED(707003, "resource_status_locked"),
        
        UNDEFINED_ERROR(-1, "undefined_error");
        
        private final int value;

        private final String reasonPhrase;


        private WebError(int value, String reasonPhrase) {
            this.value = value;
            this.reasonPhrase = reasonPhrase;
        }

        /**
         * Return the integer value of this web error code.
         */
        public int value() {
            return this.value;
        }

        /**
         * Return the reason phrase of this web error code.
         */
        public String getReasonPhrase() {
            return reasonPhrase;
        }
    }

}
