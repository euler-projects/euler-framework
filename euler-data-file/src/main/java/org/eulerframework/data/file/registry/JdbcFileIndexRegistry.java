package org.eulerframework.data.file.registry;

import org.eulerframework.data.util.ResourceEntityUtils;
import org.eulerframework.data.util.ResourceModelUtils;
import org.eulerframework.resource.ResourceScope;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

public class JdbcFileIndexRegistry implements FileIndexRegistry {
    private final static String DEFAULT_USER_ID = "anonymous";
    private final static String DEFAULT_TENANT_ID = "default";
    private final static ResourceScope DEFAULT_RS_SCOPE = ResourceScope.PUBLIC;

    private static final String INSERT_FILE_INDEX_DATA = "insert into t_file_storage_index (" +
            "id, " +
            "filename,  " +
            "extension,  " +
            "storage_type,  " +
            "storage_index,  " +
            "user_id,  " +
            "tenant_id,  " +
            "resource_scope,  " +
            "created_by,  " +
            "modified_by,  " +
            "created_date,  " +
            "modified_date) " +
            "VALUES " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_FILE_INDEX_DATA = "select " +
            "id, " +
            "filename,  " +
            "extension,  " +
            "storage_type,  " +
            "storage_index,  " +
            "user_id,  " +
            "tenant_id,  " +
            "resource_scope,  " +
            "created_by,  " +
            "modified_by,  " +
            "created_date,  " +
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
        if (fileIndex.getUserId() == null) {
            fileIndex.setUserId(DEFAULT_USER_ID);
        }
        if (fileIndex.getTenantId() == null) {
            fileIndex.setTenantId(DEFAULT_TENANT_ID);
        }
        if (fileIndex.getResourceScope() == null) {
            fileIndex.setResourceScope(DEFAULT_RS_SCOPE);
        }
        if (fileIndex.getCreatedBy() == null) {
            fileIndex.setCreatedBy(DEFAULT_USER_ID);
        }
        if (fileIndex.getLastModifiedBy() == null) {
            fileIndex.setLastModifiedBy(DEFAULT_USER_ID);
        }
        fileIndex.setCreatedDate(new Date());
        fileIndex.setLastModifiedDate(fileIndex.getCreatedDate());

        this.jdbcOperations.update(INSERT_FILE_INDEX_DATA, ps -> {
            int index = 0;
            ps.setString(++index, fileIndex.getFileId());
            ps.setString(++index, fileIndex.getFilename());
            ps.setString(++index, fileIndex.getExtension());
            ps.setString(++index, fileIndex.getStorageType());
            ps.setString(++index, fileIndex.getStorageIndex());
            index = ResourceEntityUtils.updatePreparedStatement(fileIndex, ps, index);
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
                    ResourceModelUtils.updateModelResourceFields(rs, fileIndex);
                    return fileIndex;
                });
    }
}
