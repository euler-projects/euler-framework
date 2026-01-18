package org.eulerframework.data.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eulerframework.data.file.registry.FileIndexRegistry;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.BiFunction;

public class LocalFileStorage extends AbstractLocalFileStorage {
    public final static String TYPE = "local";

    private static final String INSERT = "insert into t_file_storage_local (id, prefix, saved_name, size) VALUES (?, ?, ?, ?)";
    private static final String SELECT_SIZE = "select size from t_file_storage_local where id = ?";

    private final String baseDir;

    public LocalFileStorage(JdbcOperations jdbcOperations, String fileDownloadUrlTemplate, String baseDir, FileIndexRegistry fileIndexRegistry) {
        super(jdbcOperations, fileDownloadUrlTemplate, fileIndexRegistry);

        this.baseDir = baseDir;
    }

    public LocalFileStorage(
            JdbcOperations jdbcOperations,
            String fileDownloadUrlTemplate,
            FileIndexRegistry fileIndexRegistry,
            BiFunction<JdbcOperations, String, Integer> fileSizeLoader,
            String baseDir) {
        super(jdbcOperations, fileDownloadUrlTemplate, fileIndexRegistry, fileSizeLoader);

        this.baseDir = baseDir;
    }

    @Override
    protected String saveFileData(File file, String filename) throws IOException {
        Assert.notNull(file, "argument file is required.");
        try (InputStream inputStream = FileUtils.openInputStream(file)) {
            return this.saveFileData(inputStream, StringUtils.hasText(filename) ? filename : file.getName());
        }
    }

    @Override
    protected String saveFileData(InputStream in, String filename) throws IOException {
        Assert.notNull(in, "argument in is required.");
        Assert.hasText(filename, "argument filename is required.");

        String datePrefix = Instant.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String savedFilename = UUID.randomUUID().toString();
        int size;

        File file = FileUtils.getFile(
                this.baseDir,
                Instant.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE),
                savedFilename);

        FileUtils.createParentDirectories(file);

        try (FileOutputStream out = FileUtils.openOutputStream(file)) {
            size = IOUtils.copy(in, out);
        }

        String storageIndex = UUID.randomUUID().toString();
        this.getJdbcOperations().update(
                INSERT,
                ps -> {
                    ps.setString(1, storageIndex);
                    ps.setString(2, datePrefix);
                    ps.setString(3, filename);
                    ps.setInt(4, size);
                }
        );

        return storageIndex;
    }

    @Override
    protected void writeFileData(String fileIndex, File dest) throws IOException {

    }

    @Override
    protected void writeFileData(String fileIndex, OutputStream out) throws IOException {

    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean support(String type) {
        return false;
    }

    @Override
    BiFunction<JdbcOperations, String, Integer> defaultFileSizeLoader() {
        return (jdbcOperations, fileIndex) -> jdbcOperations.query(
                SELECT_SIZE,
                ps -> ps.setString(1, fileIndex),
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    return rs.getInt("size");
                });
    }
}
