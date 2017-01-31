package net.eulerframework.web.module.authentication.exception.signup;

@SuppressWarnings("serial")
public class IncorrectUsernameFormatException extends SignUpException {

    public IncorrectUsernameFormatException() {
        super("INCORRECT_USERNAME_FORMAT", 0);
    }

}
