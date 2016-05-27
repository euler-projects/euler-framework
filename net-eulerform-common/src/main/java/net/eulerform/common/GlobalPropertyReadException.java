package net.eulerform.common;

@SuppressWarnings("serial")
public class GlobalPropertyReadException extends RuntimeException {

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
