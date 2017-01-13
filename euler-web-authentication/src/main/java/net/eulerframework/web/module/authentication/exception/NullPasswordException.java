package net.eulerframework.web.module.authentication.exception;

public class NullPasswordException extends UserCheckException {

    /**
     * 
     */
    private static final long serialVersionUID = 5584724892515458585L;

    @Override
    public String getViewInfo() {
        return "PASSWORD_IS_NULL";
    }

}
