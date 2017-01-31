package net.eulerframework.web.module.authentication.exception.signup;

@SuppressWarnings("serial")
public class UserEmailAlreadyUsedFormatException extends SignUpException {

    public UserEmailAlreadyUsedFormatException() {
        super("EMAIL_ALREADY_BE_USED", 0);
    }

}
