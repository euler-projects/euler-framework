package org.eulerframework.web.module.file;

import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.*;

public class JdbcFileStorage extends AbstractFileStorage {
    public static final String TYPE = "jdbc";

    public JdbcFileStorage(JdbcOperations jdbcOperations) {
        super(jdbcOperations);
    }

    @Override
    protected String saveFileData(File file, String filename) {
        return "";
    }

    @Override
    protected String saveFileData(InputStream in, String filename) throws IOException {
        byte[] data = IOUtils.toByteArray(in);
        long id = this.insertFileStorageIndex(data);
        return String.valueOf(id);
    }

    @Override
    protected void writeFileData(String fileIndex, File dest) {
    }

    @Override
    protected void writeFileData(String fileIndex, OutputStream out) throws IOException {
        byte[] data = this.selectFileStorageIndex(Long.parseLong(fileIndex));
        IOUtils.write(data, out);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    private static final String INSERT = "insert into t_file_storage_jdbc (data) VALUES (?)";
    private static final String SELECT = "select data from t_file_storage_jdbc where id = ?";

    private long insertFileStorageIndex(byte[] data) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.getJdbcOperations().update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
                    ps.setBytes(1, data);
                    return ps;
                },
                keyHolder);
        return keyHolder.getKeyAs(BigInteger.class).longValue();
    }

    private byte[] selectFileStorageIndex(long fileIndex) {
        return this.getJdbcOperations().query(SELECT,
                ps -> ps.setLong(1, fileIndex),
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    return rs.getBytes("data");
                });
    }
}
