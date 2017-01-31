package net.eulerframework.web.module.authentication.exception.signup;

@SuppressWarnings("serial")
public class IncorrectPasswordFormatException extends SignUpException {

    public IncorrectPasswordFormatException() {
        super("INCORRECT_PASSWORD_FORMAT", 0);
    }

}
