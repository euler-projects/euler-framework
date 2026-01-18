package org.eulerframework.data.file.servlet;

import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.data.file.registry.FileIndex;
import org.eulerframework.data.file.registry.FileIndexRegistry;
import org.eulerframework.web.core.exception.web.api.ResourceNotFoundException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StorageFileDownloaderChain {
    private final List<StorageFileDownloader> storageFileDownloader = new ArrayList<>();

    private final FileIndexRegistry fileIndexRegistry;

    public StorageFileDownloaderChain(FileIndexRegistry fileIndexRegistry) {
        this.fileIndexRegistry = fileIndexRegistry;
    }

    public StorageFileDownloaderChain add(StorageFileDownloader storageFileDownloader) {
        this.storageFileDownloader.add(storageFileDownloader);
        return this;
    }

    public void download(String fileId, HttpServletResponse response) throws IOException {
        FileIndex fileIndex = this.fileIndexRegistry.getFileIndex(fileId);
        if (fileIndex == null) {
            throw new ResourceNotFoundException("Storage file " + fileId + "not found");
        }

        for (StorageFileDownloader storageFileDownloader : this.storageFileDownloader) {
            if (storageFileDownloader.support(fileIndex.getStorageType())) {
                storageFileDownloader.download(fileId, response);
                break;
            }
        }

        throw new IllegalArgumentException("Can not download storage file " + fileId +
                ": can not find downloader for storage type: " + fileIndex.getStorageType());
    }
}
