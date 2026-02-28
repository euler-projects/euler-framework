package org.eulerframework.data.file.registry;

public interface FileIndexRegistry {

    FileIndex createFileIndex(FileIndex fileIndex);

    FileIndex getFileIndex(String fileId);
}
