package net.eulerframework.web.core.exception.web;

@SuppressWarnings("serial")
public class DefaultViewException extends ViewException {
    
    public DefaultViewException() {
        super(WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value());
    }
    
    public DefaultViewException(Throwable e) {
        super(WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value(), e);
    }
    
    public DefaultViewException(String message) {
        super(message, WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value());
    }
    
    public DefaultViewException(String message, Throwable e) {
        super(message, WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value(), e);
    }
}
