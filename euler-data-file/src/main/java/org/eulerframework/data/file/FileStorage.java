package org.eulerframework.data.file;

import org.eulerframework.core.function.Handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public interface FileStorage extends Handler<String> {

    String getType();

    StorageFile save(File file, String filename) throws IOException;

    StorageFile save(InputStream in, String filename) throws IOException;

    StorageFile getStorageFile(String fileId);

    void get(String fileId, File dest, Consumer<StorageFile> storageFileConsumer) throws IOException;

    void get(String fileId, OutputStream out, Consumer<StorageFile> storageFileConsumer) throws IOException;
}
