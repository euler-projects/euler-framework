package net.eulerframework.web.module.authentication.exception;

public class NullPasswordException extends UserCheckException {

    /**
     * 
     */
    private static final long serialVersionUID = 5584724892515458585L;

    @Override
    public String getMsg() {
        return "PASSWORD_IS_NULL";
    }

    @Override
    public int getCode() {
        // TODO Auto-generated method stub
        return 0;
    }

}
