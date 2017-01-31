package net.eulerframework.web.module.authentication.exception.signup;

@SuppressWarnings("serial")
public class IncorrectUserEmailFormatException extends SignUpException {

    public IncorrectUserEmailFormatException() {
        super("INCORRECT_EMAIL_FORMAT", 0);
    }

}
