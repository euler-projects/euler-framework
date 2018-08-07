package net.eulerframework.web.core.exception;

/**
 * @author cFrost
 *
 */
public class EulerFrameworkInitException extends RuntimeException {

    public EulerFrameworkInitException(String message) {
        super(message);
    }
    
    public EulerFrameworkInitException(String message, Throwable e) {
        super(message, e);
    }
}
