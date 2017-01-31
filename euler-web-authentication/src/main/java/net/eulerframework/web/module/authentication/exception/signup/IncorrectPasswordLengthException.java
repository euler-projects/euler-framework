package net.eulerframework.web.module.authentication.exception.signup;

@SuppressWarnings("serial")
public class IncorrectPasswordLengthException extends SignUpException {

    public IncorrectPasswordLengthException() {
        super("INCORRECT_PASSWORD_LENGTH", 0);
    }

}
