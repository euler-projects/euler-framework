package net.eulerframework.web.core.exception.web;

public class UndefinedWebException extends WebException {
    
    public UndefinedWebException() {
        super(SystemWebError.UNDEFINED_ERROR);
    }
    
    public UndefinedWebException(Throwable e) {
        super(SystemWebError.UNDEFINED_ERROR, e);
    }
    
    public UndefinedWebException(String message) {
        super(message, SystemWebError.UNDEFINED_ERROR);
    }
    
    public UndefinedWebException(String message, Throwable e) {
        super(message, SystemWebError.UNDEFINED_ERROR, e);
    }
}
