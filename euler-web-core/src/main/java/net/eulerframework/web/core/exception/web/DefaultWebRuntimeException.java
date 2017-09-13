package net.eulerframework.web.core.exception.web;

public class DefaultWebRuntimeException extends WebRuntimeException {
    
    public DefaultWebRuntimeException(WebError webError) {
        super(webError.getReasonPhrase(), webError.value());
    }
    
    public DefaultWebRuntimeException(WebError webError, Throwable e) {
        super(webError.getReasonPhrase(), webError.value(), e);
    }
    
    public DefaultWebRuntimeException(String message, WebError webError) {
        super(message, webError.getReasonPhrase(), webError.value());
    }
    
    public DefaultWebRuntimeException(String message, WebError webError, Throwable e) {
        super(message, webError.getReasonPhrase(), webError.value(), e);
    }
}
