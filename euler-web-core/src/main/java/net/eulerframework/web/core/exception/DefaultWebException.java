package net.eulerframework.web.core.exception;

@SuppressWarnings("serial")
public class DefaultWebException extends WebException {
    
    public DefaultWebException(WebError webError) {
        super(webError.getReasonPhrase(), webError.value());
    }
    
    public DefaultWebException(WebError webError, Throwable e) {
        super(webError.getReasonPhrase(), webError.value(), e);
    }
    
    public DefaultWebException(String message, WebError webError) {
        super(message, webError.getReasonPhrase(), webError.value());
    }
    
    public DefaultWebException(String message, WebError webError, Throwable e) {
        super(message, webError.getReasonPhrase(), webError.value(), e);
    }
}
