package net.eulerframework.web.core.exception.web;

@SuppressWarnings("serial")
public class DefaultViewException extends ViewException {
    
    public DefaultViewException() {
        super(Error.UNDEFINED_ERROR.getReasonPhrase(), Error.UNDEFINED_ERROR.value());
    }
    
    public DefaultViewException(Throwable e) {
        super(Error.UNDEFINED_ERROR.getReasonPhrase(), Error.UNDEFINED_ERROR.value(), e);
    }
    
    public DefaultViewException(String message) {
        super(message, Error.UNDEFINED_ERROR.getReasonPhrase(), Error.UNDEFINED_ERROR.value());
    }
    
    public DefaultViewException(String message, Throwable e) {
        super(message, Error.UNDEFINED_ERROR.getReasonPhrase(), Error.UNDEFINED_ERROR.value(), e);
    }
}
