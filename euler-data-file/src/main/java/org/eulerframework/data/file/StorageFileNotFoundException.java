package org.eulerframework.data.file;

import org.eulerframework.exception.EulerException;

public class StorageFileNotFoundException extends EulerException {
    public StorageFileNotFoundException(String message) {
        super(message);
    }
}
