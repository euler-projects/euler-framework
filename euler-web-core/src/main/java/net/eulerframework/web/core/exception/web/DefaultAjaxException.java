package net.eulerframework.web.core.exception.web;

@SuppressWarnings("serial")
public class DefaultAjaxException extends AjaxException {
    
    private final static String ERROR = "unknown_error";
    private final static int CODE = -1;
    
    public DefaultAjaxException() {
        super(ERROR, CODE);
    }
    
    public DefaultAjaxException(Throwable e) {
        super(ERROR, CODE, e);
    }
    
    public DefaultAjaxException(String message) {
        super(message, ERROR, CODE);
    }
    
    public DefaultAjaxException(String message, Throwable e) {
        super(message, ERROR, CODE, e);
    }
}
