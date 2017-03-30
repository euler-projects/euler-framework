package net.eulerframework.web.core.exception.web;

@SuppressWarnings("serial")
public class DefaultAjaxException extends AjaxException {
    
    public DefaultAjaxException() {
        super(WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value());
    }
    
    public DefaultAjaxException(Throwable e) {
        super(WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value(), e);
    }
    
    public DefaultAjaxException(String message) {
        super(message, WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value());
    }
    
    public DefaultAjaxException(String message, Throwable e) {
        super(message, WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value(), e);
    }
}
