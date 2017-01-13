package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class UserEmailAlreadyUsedFormatException extends UserCheckException {

    @Override
    public String getViewInfo() {
        return "EMAIL_ALREADY_BE_USED";
    }

}
