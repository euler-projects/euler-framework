/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.data.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.io.*;
import java.math.BigInteger;
import java.sql.*;
import java.util.function.BiFunction;

public class JdbcFileStorage extends AbstractFileStorage {
    public final static String TYPE = "jdbc";
    public final static String ATTR_FILE_SIZE = "fileSize";

    private static final String INSERT = "insert into t_file_storage_jdbc (size, data) VALUES (?, ?)";
    private static final String SELECT_SIZE = "select size from t_file_storage_jdbc where id = ?";
    private static final String SELECT_DATA = "select data from t_file_storage_jdbc where id = ?";

    private final BiFunction<JdbcOperations, byte[], Long> fileDataSaver;
    private final BiFunction<JdbcOperations, String, Integer> fileSizeLoader;
    private final BiFunction<JdbcOperations, String, byte[]> fileDataLoader;

    public JdbcFileStorage(JdbcOperations jdbcOperations) {
        super(jdbcOperations);
        this.fileDataSaver = defaultFileDataSaver();
        this.fileSizeLoader = defaultFileSizeLoader();
        this.fileDataLoader = defaultFileDataLoader();
    }

    public JdbcFileStorage(
            JdbcOperations jdbcOperations,
            FileIndexDataSaver fileIndexDataSaver,
            BiFunction<JdbcOperations, String, FileIndex> fileIndexDataLoader,
            BiFunction<JdbcOperations, byte[], Long> fileDataSaver,
            BiFunction<JdbcOperations, String, Integer> fileSizeLoader,
            BiFunction<JdbcOperations, String, byte[]> fileDataLoader) {
        super(jdbcOperations, fileIndexDataSaver, fileIndexDataLoader);
        this.fileDataSaver = fileDataSaver;
        this.fileSizeLoader = fileSizeLoader;
        this.fileDataLoader = fileDataLoader;
    }

    @Override
    public boolean support(String type) {
        return TYPE.equals(type);
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
        long id = this.fileDataSaver.apply(this.getJdbcOperations(), data);
        return String.valueOf(id);
    }

    @Override
    protected void getAttributes(FileIndex storageFile) {
        Integer fileSize = this.fileSizeLoader.apply(this.getJdbcOperations(), storageFile.getStorageIndex());
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
        byte[] data = this.fileDataLoader.apply(this.getJdbcOperations(), fileIndex);
        IOUtils.write(data, out);
    }

    private static BiFunction<JdbcOperations, byte[], Long> defaultFileDataSaver() {
        return (jdbcOperations, data) -> {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcOperations.update(
                    con -> {
                        PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
                        ps.setInt(1, data.length);
                        ps.setBytes(2, data);
                        return ps;
                    },
                    keyHolder);
            BigInteger key = (BigInteger) keyHolder.getKey();
            if (key == null) {
                throw new IllegalStateException("Primary key is null");
            }
            return key.longValue();
        };
    }

    private static BiFunction<JdbcOperations, String, Integer> defaultFileSizeLoader() {
        return (jdbcOperations, fileIndex) -> jdbcOperations.query(
                SELECT_SIZE,
                ps -> ps.setLong(1, Long.parseLong(fileIndex)),
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    return rs.getInt("size");
                });
    }

    private static BiFunction<JdbcOperations, String, byte[]> defaultFileDataLoader() {
        return (jdbcOperations, fileIndex) -> jdbcOperations.query(
                SELECT_DATA,
                ps -> ps.setLong(1, Long.parseLong(fileIndex)),
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    return rs.getBytes("data");
                });
    }
}
