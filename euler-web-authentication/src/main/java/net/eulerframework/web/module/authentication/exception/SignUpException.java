package net.eulerframework.web.module.authentication.exception;

import net.eulerframework.web.core.exception.web.ViewException;

@SuppressWarnings("serial")
public class SignUpException extends ViewException {

    public SignUpException(String message) {
        super(message, -101);
    }
}
