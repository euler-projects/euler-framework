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

import org.apache.commons.io.FilenameUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class AbstractFileStorage implements FileStorage {

    private static final String INSERT_FILE_INDEX_DATA = "insert into t_file_storage_index (" +
            "id, " +
            "filename,  " +
            "extension,  " +
            "storage_type,  " +
            "storage_index,  " +
            "tenant_id,  " +
            "created_by,  " +
            "created_date,  " +
            "modified_by,  " +
            "modified_date) " +
            "VALUES " +
            "(?, ?, ?, ?, ?, '1', '1', now(), '1', now())";
    private static final String SELECT_FILE_INDEX_DATA = "select " +
            "id, " +
            "filename,  " +
            "extension,  " +
            "storage_type,  " +
            "storage_index,  " +
            "tenant_id,  " +
            "created_by,  " +
            "created_date,  " +
            "modified_by,  " +
            "modified_date " +
            "from t_file_storage_index " +
            "where id = ?";

    private final JdbcOperations jdbcOperations;

    private final FileIndexDataSaver fileIndexDataSaver;
    private final BiFunction<JdbcOperations, String, FileIndex> fileIndexDataLoader;

    public AbstractFileStorage(JdbcOperations jdbcOperations) {
        this(jdbcOperations, defaultFileIndexDataSaver(), defaultFileIndexDataLoader());
    }

    public AbstractFileStorage(JdbcOperations jdbcOperations, FileIndexDataSaver fileIndexDataSaver, BiFunction<JdbcOperations, String, FileIndex> fileIndexDataLoader) {
        this.jdbcOperations = jdbcOperations;
        this.fileIndexDataSaver = fileIndexDataSaver;
        this.fileIndexDataLoader = fileIndexDataLoader;
    }

    protected JdbcOperations getJdbcOperations() {
        return jdbcOperations;
    }

    protected abstract String saveFileData(File file, String filename) throws IOException;

    /**
     * 读取并保存一个 {@link InputStream} 中的全部数据
     *
     * @param in       {@link InputStream} 对象, <code>FileStorage</code> 将读取其全部数据作为一个文件存储
     * @param filename 原始文件名, 由于无法从 {@link InputStream} 中获取文件的原始文件名, 所以要从过此参数传入
     * @return 保存后的文件索引, <code>FileStorage</code> 将会存储此索引,
     * 并在需要的时候用此索引从具体的 <code>FileStorage</code> 实现获取对应文件
     */
    protected abstract String saveFileData(InputStream in, String filename) throws IOException;

    protected abstract void applyAttributes(FileIndex storageFile);

    protected abstract void writeFileData(String fileIndex, File dest) throws IOException;

    protected abstract void writeFileData(String fileIndex, OutputStream out) throws IOException;

    @Override
    @Transactional
    public FileIndex save(File file, String filename) throws IOException {
        String fileId = UUID.randomUUID().toString();
        this.fileIndexDataSaver.save(
                this.jdbcOperations,
                fileId,
                filename,
                FilenameUtils.getExtension(filename),
                this.getType(),
                this.saveFileData(file, filename));
        return this.getStorageIndex(fileId);
    }

    @Override
    @Transactional
    public FileIndex save(InputStream in, String filename) throws IOException {
        String fileId = UUID.randomUUID().toString();
        this.fileIndexDataSaver.save(
                this.jdbcOperations,
                fileId,
                filename,
                FilenameUtils.getExtension(filename),
                this.getType(),
                this.saveFileData(in, filename));
        return this.getStorageIndex(fileId);
    }

    @Override
    public FileIndex getStorageIndex(String fileId) {
        String baseName = FilenameUtils.getBaseName(fileId);
        String exceptedExtension = FilenameUtils.getExtension(fileId);

        FileIndex storageFile = this.fileIndexDataLoader.apply(this.jdbcOperations, baseName);

        if (storageFile == null) {
            return null;
        }

        if (StringUtils.hasText(exceptedExtension) && !exceptedExtension.equalsIgnoreCase(storageFile.getExtension())) {
            return null;
        }

        this.applyAttributes(storageFile);
        return storageFile;
    }

    @Override
    public void get(String fileId, File dest, Consumer<FileIndex> storageFileConsumer) throws IOException, StorageFileNotFoundException {
        FileIndex storageFile = this.getStorageIndex(fileId);
        if (storageFile == null) {
            throw new StorageFileNotFoundException("Storage file '" + fileId + "' not exists");
        }
        storageFileConsumer.accept(storageFile);
        this.writeFileData(storageFile.getStorageIndex(), dest);
    }

    @Override
    public void get(String fileId, OutputStream out, Consumer<FileIndex> storageFileConsumer) throws IOException, StorageFileNotFoundException {
        FileIndex fileIndex = this.getStorageIndex(fileId);
        if (fileIndex == null) {
            throw new StorageFileNotFoundException("Storage file '" + fileId + "' not exists");
        }
        storageFileConsumer.accept(fileIndex);
        this.writeFileData(fileIndex.getStorageIndex(), out);
    }

    private static FileIndexDataSaver defaultFileIndexDataSaver() {
        return (jdbcOperations, fileId, filename, extension, storageType, storageIndex) ->
                jdbcOperations.update(INSERT_FILE_INDEX_DATA, ps -> {
                    int index = 0;
                    ps.setString(++index, fileId);
                    ps.setString(++index, filename);
                    ps.setString(++index, extension);
                    ps.setString(++index, storageType);
                    ps.setString(++index, storageIndex);
                });
    }

    private static BiFunction<JdbcOperations, String, FileIndex> defaultFileIndexDataLoader() {
        return (jdbcOperations, fileId) -> jdbcOperations.query(SELECT_FILE_INDEX_DATA,
                ps -> ps.setString(1, fileId),
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    FileIndex fileIndex = new FileIndex();
                    fileIndex.setFileId(rs.getString("id"));
                    fileIndex.setFilename(rs.getString("filename"));
                    fileIndex.setExtension(rs.getString("extension"));
                    fileIndex.setStorageType(rs.getString("storage_type"));
                    fileIndex.setStorageIndex(rs.getString("storage_index"));
                    fileIndex.setTenantId(rs.getString("tenant_id"));
                    fileIndex.setCreatedBy(rs.getString("created_by"));
                    fileIndex.setCreatedDate(rs.getDate("created_date"));
                    fileIndex.setLastModifiedBy(rs.getString("modified_by"));
                    fileIndex.setLastModifiedDate(rs.getDate("modified_date"));
                    return fileIndex;
                });
    }

    public interface FileIndexDataSaver {
        void save(JdbcOperations jdbcOperations, String fileId, String filename, String extension, String storageType, String storageIndex);
    }
}
