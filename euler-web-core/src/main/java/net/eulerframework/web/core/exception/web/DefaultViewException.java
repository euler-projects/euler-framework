package net.eulerframework.web.core.exception.web;

@SuppressWarnings("serial")
public class DefaultViewException extends ViewException {
    
    private final static String ERROR = "unknown_error";
    private final static int CODE = -1;
    
    public DefaultViewException() {
        super(ERROR, CODE);
    }
    
    public DefaultViewException(Throwable e) {
        super(ERROR, CODE, e);
    }
    
    public DefaultViewException(String message) {
        super(message, ERROR, CODE);
    }
    
    public DefaultViewException(String message, Throwable e) {
        super(message, ERROR, CODE, e);
    }
}
