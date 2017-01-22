package net.eulerframework.web.core.exception;

@SuppressWarnings("serial")
public class DefaultAjaxException extends AjaxException {
    
    private String msg;
    private int code;
    
    public DefaultAjaxException(String displauMsg, Throwable cause) {
        this(0, displauMsg, cause);
    }
    
    public DefaultAjaxException(int errorCode, String displauMsg, Throwable cause) {
        super(cause);
        this.msg = displauMsg;
        this.code = errorCode;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }

}
