package net.eulerframework.web.module.authentication.exception.signup;

import net.eulerframework.web.core.exception.web.ViewException;

@SuppressWarnings("serial")
public abstract class SignUpException extends ViewException {

    public SignUpException(String message, int code) {
        super(message, code);
    }
}
