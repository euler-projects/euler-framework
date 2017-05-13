package net.eulerframework.web.core.exception.web;

@SuppressWarnings("serial")
public class UndefinedWebException extends WebException {
    
    public UndefinedWebException() {
        super(WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value());
    }
    
    public UndefinedWebException(Throwable e) {
        super(WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value(), e);
    }
    
    public UndefinedWebException(String message) {
        super(message, WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value());
    }
    
    public UndefinedWebException(String message, Throwable e) {
        super(message, WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value(), e);
    }
}
