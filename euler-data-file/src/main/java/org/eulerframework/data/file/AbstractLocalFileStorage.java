package org.eulerframework.data.file;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.common.util.net.URIBuilder;
import org.eulerframework.data.file.registry.FileIndex;
import org.eulerframework.data.file.registry.FileIndexRegistry;
import org.eulerframework.data.file.web.security.FileToken;
import org.eulerframework.data.file.web.security.FileTokenRegistry;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public abstract class AbstractLocalFileStorage extends AbstractFileStorage {
    public final static String ATTR_FILE_SIZE = "fileSize";

    private final String fileDownloadUrlTemplate;
    private final FileTokenRegistry fileTokenRegistry;

    public AbstractLocalFileStorage(String fileDownloadUrlTemplate, FileIndexRegistry fileIndexRegistry, FileTokenRegistry fileTokenRegistry) {
        super(fileIndexRegistry);

        this.fileDownloadUrlTemplate = fileDownloadUrlTemplate;
        this.fileTokenRegistry = fileTokenRegistry;
    }

    abstract int getFileSize(String fileIndex);

    @Override
    protected void applyAttributes(FileIndex storageFile, Map<String, Object> options) throws IOException {
        super.applyAttributes(storageFile, options);
        storageFile.addAttribute(ATTR_FILE_SIZE, this.getFileSize(storageFile.getStorageIndex()));
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
