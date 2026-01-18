package org.eulerframework.data.file;

import org.eulerframework.data.file.registry.FileIndex;
import org.eulerframework.data.file.registry.FileIndexRegistry;
import org.springframework.jdbc.core.JdbcOperations;

import java.net.URI;
import java.util.function.BiFunction;

public abstract class AbstractLocalFileStorage extends AbstractFileStorage {
    public final static String ATTR_FILE_SIZE = "fileSize";

    private final BiFunction<JdbcOperations, String, Integer> fileSizeLoader;
    private final String fileDownloadUrlTemplate;

    public AbstractLocalFileStorage(JdbcOperations jdbcOperations, String fileDownloadUrlTemplate, FileIndexRegistry fileIndexRegistry) {
        super(jdbcOperations, fileIndexRegistry);
        this.fileSizeLoader = defaultFileSizeLoader();

        this.fileDownloadUrlTemplate = fileDownloadUrlTemplate;
    }

    public AbstractLocalFileStorage(
            JdbcOperations jdbcOperations,
            String fileDownloadUrlTemplate,
            FileIndexRegistry fileIndexRegistry,
            BiFunction<JdbcOperations, String, Integer> fileSizeLoader) {
        super(jdbcOperations, fileIndexRegistry);
        this.fileSizeLoader = fileSizeLoader;

        this.fileDownloadUrlTemplate = fileDownloadUrlTemplate;
    }

    abstract BiFunction<JdbcOperations, String, Integer> defaultFileSizeLoader();

    @Override
    protected void applyAttributes(FileIndex storageFile) {
        Integer fileSize = this.fileSizeLoader.apply(this.getJdbcOperations(), storageFile.getStorageIndex());
        storageFile.addAttribute(ATTR_FILE_SIZE, fileSize);
    }

    @Override
    public URI getUri(String fileId) {
        String url = this.fileDownloadUrlTemplate.replace("{fileId}", fileId);
        return URI.create(url);
    }
}
