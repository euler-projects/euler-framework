package org.eulerframework.data.file;

import org.apache.commons.io.FilenameUtils;
import org.eulerframework.web.core.exception.web.api.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractFileStorage implements FileStorage {

    private final JdbcOperations jdbcOperations;

    public AbstractFileStorage(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    protected JdbcOperations getJdbcOperations() {
        return jdbcOperations;
    }

    protected abstract String saveFileData(File file, String filename) throws IOException;

    protected abstract String saveFileData(InputStream in, String filename) throws IOException;

    protected abstract void getAttributes(StorageFile storageFile);

    protected abstract void writeFileData(String fileIndex, File dest) throws IOException;

    protected abstract void writeFileData(String fileIndex, OutputStream out) throws IOException;

    @Override
    @Transactional
    public StorageFile save(File file, String filename) throws IOException {
        String fileId = UUID.randomUUID().toString();
        this.insertFileStorageIndex(fileId, filename, this.saveFileData(file, filename));
        return this.getStorageFile(fileId);
    }

    @Override
    @Transactional
    public StorageFile save(InputStream in, String filename) throws IOException {
        String fileId = UUID.randomUUID().toString();
        this.insertFileStorageIndex(fileId, filename, this.saveFileData(in, filename));
        return this.getStorageFile(fileId);
    }

    @Override
    public StorageFile getStorageFile(String fileId) {
        StorageFile storageFile = this.selectFileStorageIndex(fileId);
        this.getAttributes(storageFile);
        return storageFile;
    }

    @Override
    public void get(String fileId, File dest, Consumer<StorageFile> storageFileConsumer) throws IOException {
        StorageFile storageFile = this.getStorageFile(fileId);
        if (storageFile == null) {
            throw new ResourceNotFoundException("Storage file not found: " + fileId);
        }
        storageFileConsumer.accept(storageFile);
        this.writeFileData(storageFile.getStorageIndex(), dest);
    }

    @Override
    public void get(String fileId, OutputStream out, Consumer<StorageFile> storageFileConsumer) throws IOException {
        StorageFile storageFile = this.getStorageFile(fileId);
        if (storageFile == null) {
            throw new ResourceNotFoundException("Storage file not found: " + fileId);
        }
        storageFileConsumer.accept(storageFile);
        this.writeFileData(storageFile.getStorageIndex(), out);
    }

    private static final String INSERT = "insert into t_file_storage_index (id, filename,  extension,  storage_type,  storage_index,  tenant_id,  created_by,  created_date,  modified_by,  modified_date) VALUES (?, ?, ?, ?, ?, '1', '1', now(), '1', now())";
    private static final String SELECT = "select id, filename,  extension,  storage_type,  storage_index,  tenant_id,  created_by,  created_date,  modified_by,  modified_date from t_file_storage_index where id = ?";

    private void insertFileStorageIndex(String fileId, String filename, String fileIndex) {
        this.jdbcOperations.update(INSERT, ps -> {
            int index = 0;
            ps.setString(++index, fileId);
            ps.setString(++index, filename);
            ps.setString(++index, FilenameUtils.getExtension(filename));
            ps.setString(++index, this.getType());
            ps.setString(++index, fileIndex);
        });
    }

    protected StorageFile selectFileStorageIndex(String fileId) {
        return this.jdbcOperations.query(SELECT,
                ps -> ps.setString(1, fileId),
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    StorageFile storageFile = new StorageFile();
                    storageFile.setFileId(rs.getString("id"));
                    storageFile.setFilename(rs.getString("filename"));
                    storageFile.setExtension(rs.getString("extension"));
                    storageFile.setStorageType(rs.getString("storage_type"));
                    storageFile.setStorageIndex(rs.getString("storage_index"));
                    storageFile.setTenantId(rs.getString("tenant_id"));
                    storageFile.setCreatedBy(rs.getString("created_by"));
                    storageFile.setCreatedDate(rs.getDate("created_date"));
                    storageFile.setLastModifiedBy(rs.getString("modified_by"));
                    storageFile.setLastModifiedDate(rs.getDate("modified_date"));
                    return storageFile;
                });
    }
}
