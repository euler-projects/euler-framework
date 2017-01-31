package net.eulerframework.web.module.authentication.exception.signup;

@SuppressWarnings("serial")
public class NullUserEmailException extends SignUpException {

    public NullUserEmailException() {
        super("EMAIL_IS_NULL", 0);
    }

}
