package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class NullUserNameException extends UserCheckException {

    @Override
    public String getViewInfo() {
        return "USERNAME_IS_NULL";
    }

}
