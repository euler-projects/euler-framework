package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class IncorrectUserEmailFormatException extends UserCheckException {

    @Override
    public String getMsg() {
        return "INCORRECT_EMAIL_FORMAT";
    }

    @Override
    public int getCode() {
        // TODO Auto-generated method stub
        return 0;
    }

}
