package net.eulerframework.common.util;

public class NullValueException extends Exception {
    private static final long serialVersionUID = -3749594067077482264L;


    public NullValueException() {
        super();
    }

    public NullValueException(String message) {
        super(message);
    }

    
    public NullValueException(String message, Throwable cause) {
        super(message, cause);
    }

  
    public NullValueException(Throwable cause) {
        super(cause);
    }
}
