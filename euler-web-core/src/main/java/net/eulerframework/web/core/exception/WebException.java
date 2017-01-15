package net.eulerframework.web.core.exception;

@SuppressWarnings("serial")
public abstract class WebException extends RuntimeException {
    public WebException() {
        super();
    }

    public WebException(String message) {
        super(message);
    }

    public WebException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebException(Throwable cause) {
        super(cause);
    }
    
    protected WebException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }
    
    /**
     * 获取错误编码，通常应在开发时统一规划
     * @return 错误编码
     */
    public abstract int getCode();
    
    /**
     * 获取用于呈现给用户的错误信息，通常应采用大写字母+下划线方便国际化显示
     * @return 错误信息
     */
    public abstract String getMsg();

}
