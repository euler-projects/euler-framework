package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class NullUserEmailException extends UserCheckException {

    @Override
    public String getMsg() {
        return "EMAIL_IS_NULL";
    }

    @Override
    public int getCode() {
        // TODO Auto-generated method stub
        return 0;
    }

}
