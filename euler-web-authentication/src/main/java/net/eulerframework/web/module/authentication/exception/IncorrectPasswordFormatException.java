package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class IncorrectPasswordFormatException extends UserCheckException {

    @Override
    public String getViewInfo() {
        return "INCORRECT_PASSWORD_FORMAT";
    }

}
