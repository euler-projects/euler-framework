package net.eulerframework.common.util;

@SuppressWarnings("serial")
public class GlobalPropertyReadException extends Exception {

    public GlobalPropertyReadException() {
        super();
    }

    public GlobalPropertyReadException(String message) {
        super(message);
    }

    
    public GlobalPropertyReadException(String message, Throwable cause) {
        super(message, cause);
    }

  
    public GlobalPropertyReadException(Throwable cause) {
        super(cause);
    }
}
