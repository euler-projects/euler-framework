package org.eulerframework.data.file.servlet;

import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.data.file.JdbcFileStorage;
import org.eulerframework.web.util.ServletUtils;

import java.io.IOException;
import java.util.Optional;

public class JdbcStorageFileDownloader implements StorageFileDownloader {

    private final JdbcFileStorage jdbcFileStorage;

    public JdbcStorageFileDownloader(JdbcFileStorage jdbcFileStorage) {
        this.jdbcFileStorage = jdbcFileStorage;
    }

    @Override
    public void download(String fileId, HttpServletResponse response) throws IOException {
        this.jdbcFileStorage.get(fileId, response.getOutputStream(), storageFile -> ServletUtils.writeFileHeader(
                response,
                storageFile.getFilename(),
                Optional.ofNullable(storageFile.getAttribute(JdbcFileStorage.ATTR_FILE_SIZE))
                        .map(v -> (Integer) v)
                        .map(Integer::longValue)
                        .orElse(null)
        ));
    }

    @Override
    public boolean support(String type) {
        return jdbcFileStorage.support(type);
    }
}
