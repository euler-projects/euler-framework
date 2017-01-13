package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class IncorrectUserEmailFormatException extends UserCheckException {

    @Override
    public String getViewInfo() {
        return "INCORRECT_EMAIL_FORMAT";
    }

}
