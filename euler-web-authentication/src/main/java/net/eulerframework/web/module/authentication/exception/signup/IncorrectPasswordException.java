package net.eulerframework.web.module.authentication.exception.signup;

@SuppressWarnings("serial")
public class IncorrectPasswordException extends SignUpException {

    public IncorrectPasswordException() {
        super("INCORRECT_PASSWORD", 0);
    }
}
