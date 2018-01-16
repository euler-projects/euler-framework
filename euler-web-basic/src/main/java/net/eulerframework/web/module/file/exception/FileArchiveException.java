package net.eulerframework.web.module.file.exception;

public class FileArchiveException extends Exception {
    
    public FileArchiveException() {
        super();
    }

    public FileArchiveException(String message) {
        super(message);
    }

    public FileArchiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileArchiveException(Throwable cause) {
        super(cause);
    }
    
    protected FileArchiveException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
