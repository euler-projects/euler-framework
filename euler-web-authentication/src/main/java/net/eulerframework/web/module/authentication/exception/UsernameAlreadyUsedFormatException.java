package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class UsernameAlreadyUsedFormatException extends UserCheckException {

    @Override
    public String getViewInfo() {
        return "USERNAME_ALREADY_BE_USED";
    }

}
