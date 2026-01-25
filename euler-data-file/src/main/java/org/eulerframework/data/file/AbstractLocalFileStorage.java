package org.eulerframework.data.file;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.common.util.net.URIBuilder;
import org.eulerframework.data.file.registry.FileIndex;
import org.eulerframework.data.file.registry.FileIndexRegistry;
import org.eulerframework.data.file.web.security.FileToken;
import org.eulerframework.data.file.web.security.FileTokenRegistry;
import org.springframework.jdbc.core.JdbcOperations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.BiFunction;

public abstract class AbstractLocalFileStorage extends AbstractFileStorage {
    public final static String ATTR_FILE_SIZE = "fileSize";

    private final BiFunction<JdbcOperations, String, Integer> fileSizeLoader;
    private final String fileDownloadUrlTemplate;
    private final FileTokenRegistry fileTokenRegistry;

    public AbstractLocalFileStorage(JdbcOperations jdbcOperations, String fileDownloadUrlTemplate, FileIndexRegistry fileIndexRegistry, FileTokenRegistry fileTokenRegistry) {
        super(jdbcOperations, fileIndexRegistry);
        this.fileSizeLoader = defaultFileSizeLoader();

        this.fileDownloadUrlTemplate = fileDownloadUrlTemplate;
        this.fileTokenRegistry = fileTokenRegistry;
    }

    public AbstractLocalFileStorage(
            JdbcOperations jdbcOperations,
            String fileDownloadUrlTemplate,
            FileIndexRegistry fileIndexRegistry,
            FileTokenRegistry fileTokenRegistry,
            BiFunction<JdbcOperations, String, Integer> fileSizeLoader) {
        super(jdbcOperations, fileIndexRegistry);
        this.fileSizeLoader = fileSizeLoader;

        this.fileDownloadUrlTemplate = fileDownloadUrlTemplate;
        this.fileTokenRegistry = fileTokenRegistry;
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
        FileToken token = this.fileTokenRegistry.generateToken(fileId);
        URIBuilder builder = URIBuilder.of(url);
        builder.query("access_token", token.getTokenValue());
        try {
            return builder.build();
        } catch (URISyntaxException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }
}
