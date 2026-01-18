package org.eulerframework.data.file.registry;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.Transactional;

public class JdbcFileIndexRegistry implements FileIndexRegistry {

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

    public JdbcFileIndexRegistry(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    @Transactional
    public FileIndex createFileIndex(FileIndex fileIndex) {
        this.jdbcOperations.update(INSERT_FILE_INDEX_DATA, ps -> {
            int index = 0;
            ps.setString(++index, fileIndex.getFileId());
            ps.setString(++index, fileIndex.getFilename());
            ps.setString(++index, fileIndex.getExtension());
            ps.setString(++index, fileIndex.getStorageType());
            ps.setString(++index, fileIndex.getStorageIndex());
        });
        return this.getFileIndex(fileIndex.getFileId());
    }

    @Override
    public FileIndex getFileIndex(String fileId) {
        return this.jdbcOperations.query(SELECT_FILE_INDEX_DATA,
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
}
