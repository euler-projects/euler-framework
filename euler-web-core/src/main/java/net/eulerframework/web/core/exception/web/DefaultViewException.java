package net.eulerframework.web.core.exception.web;

@SuppressWarnings("serial")
public class DefaultViewException extends ViewException {
    public DefaultViewException() {
        super("UNKNOWN_ERROR", -1);
    }
    
    public DefaultViewException(String message) {
        super(message, -1);
    }
    
    public DefaultViewException(String message, Throwable e) {
        super(message, -1, e);
    }
}
