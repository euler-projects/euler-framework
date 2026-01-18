package org.eulerframework.data.file;

import org.springframework.jdbc.core.JdbcOperations;

import java.net.URI;
import java.util.function.BiFunction;

public abstract class AbstractLocalFileStorage extends AbstractFileStorage {
    public final static String ATTR_FILE_SIZE = "fileSize";

    private final BiFunction<JdbcOperations, String, Integer> fileSizeLoader;
    private final String fileDownloadUrlTemplate;

    public AbstractLocalFileStorage(JdbcOperations jdbcOperations, String fileDownloadUrlTemplate) {
        super(jdbcOperations);
        this.fileSizeLoader = defaultFileSizeLoader();

        this.fileDownloadUrlTemplate = fileDownloadUrlTemplate;
    }

    public AbstractLocalFileStorage(
            JdbcOperations jdbcOperations,
            String fileDownloadUrlTemplate,
            FileIndexDataSaver fileIndexDataSaver,
            BiFunction<JdbcOperations, String, FileIndex> fileIndexDataLoader,
            BiFunction<JdbcOperations, String, Integer> fileSizeLoader) {
        super(jdbcOperations, fileIndexDataSaver, fileIndexDataLoader);
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
