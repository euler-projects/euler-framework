package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class UserEmailAlreadyUsedFormatException extends UserCheckException {

    @Override
    public String getMsg() {
        return "EMAIL_ALREADY_BE_USED";
    }

    @Override
    public int getCode() {
        // TODO Auto-generated method stub
        return 0;
    }

}
