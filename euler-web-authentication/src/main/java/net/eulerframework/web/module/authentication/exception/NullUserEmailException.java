package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class NullUserEmailException extends UserCheckException {

    @Override
    public String getViewInfo() {
        return "EMAIL_IS_NULL";
    }

}
