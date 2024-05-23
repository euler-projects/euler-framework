package org.eulerframework.web.module.file;

import org.apache.commons.io.FilenameUtils;
import org.eulerframework.web.core.exception.web.api.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.sql.Date;
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

    protected abstract String saveFileData(File file, String filename);

    protected abstract String saveFileData(InputStream in, String filename) throws IOException;

    protected abstract void writeFileData(String fileIndex, File dest);

    protected abstract void writeFileData(String fileIndex, OutputStream out) throws IOException;

    @Override
    @Transactional
    public StorageFile save(File file, String filename) {
        StorageFile storageFile = new StorageFile();
        storageFile.setFilename(filename);
        storageFile.setExtension(FilenameUtils.getExtension(filename));
        storageFile.setStorageType(this.getType());
        storageFile.setStorageIndex(this.saveFileData(file, filename));
        storageFile.setCreatedDate(new java.util.Date());
        storageFile.setLastModifiedDate(storageFile.getCreatedDate());
        return this.insertFileStorageIndex(storageFile);
    }

    @Override
    @Transactional
    public StorageFile save(InputStream in, String filename) throws IOException {
        StorageFile storageFile = new StorageFile();
        storageFile.setFileId(UUID.randomUUID().toString());
        storageFile.setFilename(filename);
        storageFile.setExtension(FilenameUtils.getExtension(filename));
        storageFile.setStorageType(this.getType());
        storageFile.setStorageIndex(this.saveFileData(in, filename));
        storageFile.setCreatedDate(new java.util.Date());
        storageFile.setLastModifiedDate(storageFile.getCreatedDate());

        storageFile.setTenantId("1");
        storageFile.setCreatedBy("1");
        storageFile.setLastModifiedBy("1");
        return this.insertFileStorageIndex(storageFile);
    }

    @Override
    public StorageFile getInfo(String fileId) {
        return this.selectFileStorageIndex(fileId);
    }

    @Override
    public void get(String fileId, File dest, Consumer<StorageFile> storageFileConsumer) {
        StorageFile storageFile = this.getInfo(fileId);
        if (storageFile == null) {
            throw new ResourceNotFoundException("Storage file not found: " + fileId);
        }

        this.writeFileData(storageFile.getStorageIndex(), dest);
    }

    @Override
    public void get(String fileId, OutputStream out, Consumer<StorageFile> storageFileConsumer) throws IOException {
        StorageFile storageFile = this.getInfo(fileId);
        if (storageFile == null) {
            throw new ResourceNotFoundException("Storage file not found: " + fileId);
        }
        storageFileConsumer.accept(storageFile);
        this.writeFileData(storageFile.getStorageIndex(), out);

    }

    private static final String INSERT = "insert into t_file_storage_index (id, filename,  extension,  storage_type,  storage_index,  tenant_id,  created_by,  created_date,  modified_by,  modified_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT = "select id, filename,  extension,  storage_type,  storage_index,  tenant_id,  created_by,  created_date,  modified_by,  modified_date from t_file_storage_index where id = ?";

    private StorageFile insertFileStorageIndex(StorageFile storageFile) {
        this.jdbcOperations.update(INSERT, ps -> {
            int index = 0;
            ps.setString(++index, storageFile.getFileId());
            ps.setString(++index, storageFile.getFilename());
            ps.setString(++index, storageFile.getExtension());
            ps.setString(++index, storageFile.getStorageType());
            ps.setString(++index, storageFile.getStorageIndex());
            ps.setString(++index, storageFile.getTenantId());
            ps.setString(++index, storageFile.getCreatedBy());
            ps.setDate(++index, new Date(storageFile.getCreatedDate().getTime()));
            ps.setString(++index, storageFile.getLastModifiedBy());
            ps.setDate(++index, new Date(storageFile.getLastModifiedDate().getTime()));
        });
        return storageFile;
    }

    private StorageFile selectFileStorageIndex(String fileId) {
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
