package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class IncorrectUsernameFormatException extends UserCheckException {

    @Override
    public String getViewInfo() {
        return "INCORRECT_USERNAME_FORMAT";
    }

}
