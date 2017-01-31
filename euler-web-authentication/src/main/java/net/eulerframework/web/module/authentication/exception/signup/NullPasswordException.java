package net.eulerframework.web.module.authentication.exception.signup;

@SuppressWarnings("serial")
public class NullPasswordException extends SignUpException {

    public NullPasswordException() {
        super("PASSWORD_IS_NULL", 0);
    }

}
