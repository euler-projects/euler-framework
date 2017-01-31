package net.eulerframework.web.module.authentication.exception.signup;

@SuppressWarnings("serial")
public class UsernameAlreadyUsedFormatException extends SignUpException {

    public UsernameAlreadyUsedFormatException() {
        super("USERNAME_ALREADY_BE_USED", 0);
    }

}
