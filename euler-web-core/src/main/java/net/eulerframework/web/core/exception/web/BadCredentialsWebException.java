package net.eulerframework.web.core.exception.web;

/**
 * @author cFrost
 *
 */
public class BadCredentialsWebException extends WebException {
    
    public BadCredentialsWebException() {
        this("_BAD_CREDENTIALS");
    }

    public BadCredentialsWebException(String message) {
        super(message, SystemWebError.BAD_CREDENTIALS);
    }
}
