package net.eulerframework.web.core.exception;

@SuppressWarnings("serial")
public abstract class AjaxException extends WebException {
    public AjaxException() {
        super();
    }

    public AjaxException(String message) {
        super(message);
    }

    public AjaxException(String message, Throwable cause) {
        super(message, cause);
    }

    public AjaxException(Throwable cause) {
        super(cause);
    }
    
    protected AjaxException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

}
