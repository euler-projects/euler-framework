package org.eulerframework.data.file.servlet;

import org.eulerframework.data.file.registry.FileIndex;
import org.eulerframework.data.file.registry.FileIndexRegistry;
import org.eulerframework.web.core.exception.web.api.ResourceNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RangeStorageFileDownloaderChain {
    private final List<RangeStorageFileDownloader> rangeStorageFileDownloader = new ArrayList<>();

    private final FileIndexRegistry fileIndexRegistry;

    public RangeStorageFileDownloaderChain(FileIndexRegistry fileIndexRegistry) {
        this.fileIndexRegistry = fileIndexRegistry;
    }

    public RangeStorageFileDownloaderChain add(RangeStorageFileDownloader rangeStorageFileDownloader) {
        this.rangeStorageFileDownloader.add(rangeStorageFileDownloader);
        return this;
    }

    public ResponseEntity<Resource> download(String fileId) throws IOException {
        FileIndex fileIndex = this.fileIndexRegistry.getFileIndex(fileId);
        if (fileIndex == null) {
            throw new ResourceNotFoundException("Storage file " + fileId + "not found");
        }

        for (RangeStorageFileDownloader rangeStorageFileDownloader : this.rangeStorageFileDownloader) {
            if (rangeStorageFileDownloader.support(fileIndex.getStorageType())) {
                return rangeStorageFileDownloader.download(fileId);
            }
        }

        throw new IllegalArgumentException("Can not download storage file " + fileId +
                ": can not find downloader for storage type: " + fileIndex.getStorageType());
    }
}
