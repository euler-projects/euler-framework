package net.eulerframework.web.module.authentication.exception;

@SuppressWarnings("serial")
public class UserMobileAlreadyUsedFormatException extends UserCheckException {

    @Override
    public String getViewInfo() {
        return "MOBILE_ALREADY_BE_USED";
    }

}
