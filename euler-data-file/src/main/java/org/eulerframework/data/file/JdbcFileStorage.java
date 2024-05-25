package org.eulerframework.data.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.io.*;
import java.math.BigInteger;
import java.sql.*;
import java.util.function.Consumer;

public class JdbcFileStorage extends AbstractFileStorage {
    public final static String TYPE = "jdbc";
    public final static String ATTR_FILE_SIZE = "fileSize";

    public JdbcFileStorage(JdbcOperations jdbcOperations) {
        super(jdbcOperations);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    protected String saveFileData(File file, String filename) throws IOException {
        try (InputStream in = FileUtils.openInputStream(file)) {
            return this.saveFileData(in, filename);
        }
    }

    @Override
    protected String saveFileData(InputStream in, String filename) throws IOException {
        byte[] data = IOUtils.toByteArray(in);
        long id = this.insertFileData(data);
        return String.valueOf(id);
    }

    @Override
    protected void getAttributes(StorageFile storageFile) {
        Integer fileSize = this.selectFileSize(Long.parseLong(storageFile.getStorageIndex()));
        storageFile.addAttribute(ATTR_FILE_SIZE, fileSize);
    }

    @Override
    protected void writeFileData(String fileIndex, File dest) throws IOException {
        try (OutputStream out = FileUtils.openOutputStream(dest)) {
            this.writeFileData(fileIndex, out);
        }
    }

    @Override
    protected void writeFileData(String fileIndex, OutputStream out) throws IOException {
        byte[] data = this.selectFileData(Long.parseLong(fileIndex));
        IOUtils.write(data, out);
    }

    private static final String INSERT = "insert into t_file_storage_jdbc (size, data) VALUES (?, ?)";
    private static final String SELECT_SIZE = "select size from t_file_storage_jdbc where id = ?";
    private static final String SELECT_DATA = "select data from t_file_storage_jdbc where id = ?";

    protected long insertFileData(byte[] data) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.getJdbcOperations().update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, data.length);
                    ps.setBytes(2, data);
                    return ps;
                },
                keyHolder);
        return keyHolder.getKeyAs(BigInteger.class).longValue();
    }

    protected Integer selectFileSize(long fileIndex) {
        return this.getJdbcOperations().query(SELECT_SIZE,
                ps -> ps.setLong(1, fileIndex),
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    return rs.getInt("size");
                });
    }

    protected byte[] selectFileData(long fileIndex) {
        return this.getJdbcOperations().query(SELECT_DATA,
                ps -> ps.setLong(1, fileIndex),
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    return rs.getBytes("data");
                });
    }

    @Override
    public boolean support(String type) {
        return TYPE.equals(type);
    }
}
