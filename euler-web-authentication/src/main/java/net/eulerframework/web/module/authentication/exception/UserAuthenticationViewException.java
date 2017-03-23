package net.eulerframework.web.module.authentication.exception;

import net.eulerframework.web.core.exception.web.ViewException;

@SuppressWarnings("serial")
public class UserAuthenticationViewException extends ViewException {

    public UserAuthenticationViewException(String message) {
        super(message, "user_authentication_error", -101);
    }
}
