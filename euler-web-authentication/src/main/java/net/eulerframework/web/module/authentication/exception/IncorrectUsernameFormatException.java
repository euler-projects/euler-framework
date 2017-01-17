package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class IncorrectUsernameFormatException extends UserCheckException {

    @Override
    public String getMsg() {
        return "INCORRECT_USERNAME_FORMAT";
    }

    @Override
    public int getCode() {
        // TODO Auto-generated method stub
        return 0;
    }

}
