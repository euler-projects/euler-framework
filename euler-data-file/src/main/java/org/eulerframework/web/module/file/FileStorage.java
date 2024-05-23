package org.eulerframework.web.module.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public interface FileStorage {

    String getType();

    StorageFile save(File file, String filename);

    StorageFile save(InputStream in, String filename) throws IOException;

    StorageFile getInfo(String fileId);

    void get(String fileId, File dest, Consumer<StorageFile> storageFileConsumer);

    void get(String fileId, OutputStream out, Consumer<StorageFile> storageFileConsumer) throws IOException;
}
