package net.eulerframework.web.core.exception;

@SuppressWarnings("serial")
public class DefaultViewException extends ViewException {
    
    private String msg;
    private int code;
    
    public DefaultViewException(String displauMsg, Throwable cause) {
        this(0, displauMsg, cause);
    }
    
    public DefaultViewException(int errorCode, String displauMsg, Throwable cause) {
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
