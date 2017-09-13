package net.eulerframework.web.core.exception.web;

public class UndefinedWebRuntimeException extends WebRuntimeException {
    
    public UndefinedWebRuntimeException() {
        super(WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value());
    }
    
    public UndefinedWebRuntimeException(Throwable e) {
        super(WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value(), e);
    }
    
    public UndefinedWebRuntimeException(String message) {
        super(message, WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value());
    }
    
    public UndefinedWebRuntimeException(String message, Throwable e) {
        super(message, WebError.UNDEFINED_ERROR.getReasonPhrase(), WebError.UNDEFINED_ERROR.value(), e);
    }
}
