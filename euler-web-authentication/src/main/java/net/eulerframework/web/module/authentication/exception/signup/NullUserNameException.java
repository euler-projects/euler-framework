package net.eulerframework.web.module.authentication.exception.signup;

@SuppressWarnings("serial")
public class NullUserNameException extends SignUpException {

    public NullUserNameException() {
        super("USERNAME_IS_NULL", 0);
    }

}
