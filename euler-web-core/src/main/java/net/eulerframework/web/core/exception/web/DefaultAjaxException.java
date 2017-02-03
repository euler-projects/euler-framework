package net.eulerframework.web.core.exception.web;

@SuppressWarnings("serial")
public class DefaultAjaxException extends AjaxException {
    public DefaultAjaxException() {
        super("UNKNOWN_ERROR", -1);
    }
    
    public DefaultAjaxException(Throwable e) {
        super("UNKNOWN_ERROR", -1, e);
    }
    
    public DefaultAjaxException(String message, Throwable e) {
        super(message, -1, e);
    }
}
