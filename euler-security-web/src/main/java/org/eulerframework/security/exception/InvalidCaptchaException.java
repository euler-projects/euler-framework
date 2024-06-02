package org.eulerframework.security.exception;

import org.eulerframework.web.core.exception.web.WebException;

public class InvalidCaptchaException extends WebException {
    public InvalidCaptchaException() {
        super("_INVALID_CAPTCHA");
    }
}