package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class NullUserNameException extends UserCheckException {

    @Override
    public String getMsg() {
        return "USERNAME_IS_NULL";
    }

    @Override
    public int getCode() {
        // TODO Auto-generated method stub
        return 0;
    }

}
