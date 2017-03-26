package net.eulerframework.web.core.exception.web;

@SuppressWarnings("serial")
public class DefaultAjaxException extends AjaxException {
    
    public DefaultAjaxException() {
        super(Error.UNDEFINED_ERROR.getReasonPhrase(), Error.UNDEFINED_ERROR.value());
    }
    
    public DefaultAjaxException(Throwable e) {
        super(Error.UNDEFINED_ERROR.getReasonPhrase(), Error.UNDEFINED_ERROR.value(), e);
    }
    
    public DefaultAjaxException(String message) {
        super(message, Error.UNDEFINED_ERROR.getReasonPhrase(), Error.UNDEFINED_ERROR.value());
    }
    
    public DefaultAjaxException(String message, Throwable e) {
        super(message, Error.UNDEFINED_ERROR.getReasonPhrase(), Error.UNDEFINED_ERROR.value(), e);
    }
}
