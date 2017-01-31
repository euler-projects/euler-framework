package net.eulerframework.web.module.authentication.exception.signup;

@SuppressWarnings("serial")
public class UserMobileAlreadyUsedFormatException extends SignUpException {

    public UserMobileAlreadyUsedFormatException() {
        super("MOBILE_ALREADY_BE_USED", 0);
    }

}
