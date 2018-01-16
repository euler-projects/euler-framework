package net.eulerframework.web.core.exception.web;

public class UndefinedWebRuntimeException extends WebRuntimeException {
    
    public UndefinedWebRuntimeException() {
        super(SystemWebError.UNDEFINED_ERROR);
    }
    
    public UndefinedWebRuntimeException(Throwable e) {
        super(SystemWebError.UNDEFINED_ERROR, e);
    }
    
    public UndefinedWebRuntimeException(String message) {
        super(message, SystemWebError.UNDEFINED_ERROR);
    }
    
    public UndefinedWebRuntimeException(String message, Throwable e) {
        super(message, SystemWebError.UNDEFINED_ERROR, e);
    }
}
