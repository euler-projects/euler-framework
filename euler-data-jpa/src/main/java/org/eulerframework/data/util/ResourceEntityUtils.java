package org.eulerframework.data.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.data.convert.ResourceScopeConverter;
import org.eulerframework.data.entity.ResourceEntity;
import org.eulerframework.model.AbstractResourceModel;
import org.eulerframework.resource.ResourceScope;
import org.eulerframework.security.core.userdetails.EulerUserDetails;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

public class ResourceEntityUtils {
    public static void updateResourceEntity(AbstractResourceModel model, ResourceEntity entity) {
        AuditingEntityUtils.updatePrincipalAuditingEntity(model, entity);
        entity.setTenantId(model.getTenantId());
        entity.setUserId(model.getUserId());
        entity.setResourceScope(model.getResourceScope());
    }

    /**
     * Populates resource metadata fields in a {@link PreparedStatement} according to the standard column order
     * defined for resource entities.
     *
     * <p><strong>Null Handling:</strong> If any attribute of the model is {@code null}, the corresponding database
     * column will be set to {@code NULL}. Callers must ensure proper null handling, especially when the target
     * database columns are defined as {@code NOT NULL}.
     *
     * <p><strong>Standard Column Order:</strong> The SQL statement must define resource columns in the exact
     * sequence shown below. This convention ensures consistency across all resource tables in the system:
     *
     * <pre>{@code
     * CREATE TABLE t_story (
     *     ...
     *     user_id VARCHAR(36),
     *     tenant_id VARCHAR(36),
     *     resource_scope INT UNSIGNED,
     *     created_by VARCHAR(36),
     *     modified_by VARCHAR(36),
     *     created_date DATETIME(3),
     *     modified_date DATETIME(3),
     *     ...
     * )
     * }</pre>
     *
     * @param model          the resource model containing metadata values to be persisted
     * @param ps             the {@link PreparedStatement} to populate with resource field values
     * @param parameterIndex the current parameter index position in the {@link PreparedStatement}
     * @return the updated parameter index after all resource fields have been set
     * @throws SQLException if {@code parameterIndex} does not correspond to a valid parameter marker
     *                      in the SQL statement; if a database access error occurs; or if this method
     *                      is invoked on a closed {@link PreparedStatement}
     */
    public static int updatePreparedStatement(AbstractResourceModel model, PreparedStatement ps, int parameterIndex) throws SQLException {
        ps.setString(++parameterIndex, model.getUserId());
        ps.setString(++parameterIndex, model.getTenantId());
        ps.setObject(++parameterIndex, Optional.ofNullable(model.getResourceScope()).map(ResourceScopeConverter.INSTANCE::convertToDatabaseColumn).orElse(null), Types.INTEGER);
        ps.setString(++parameterIndex, model.getCreatedBy());
        ps.setString(++parameterIndex, model.getLastModifiedBy());
        ps.setDate(++parameterIndex, Optional.ofNullable(model.getCreatedDate()).map(java.util.Date::getTime).map(java.sql.Date::new).orElse(null));
        ps.setDate(++parameterIndex, Optional.ofNullable(model.getLastModifiedDate()).map(java.util.Date::getTime).map(java.sql.Date::new).orElse(null));
        return parameterIndex;
    }

    public static <E extends ResourceEntity> E newResourceEntity(String userId, Class<E> clazz) {
        return newResourceEntity(userId, EulerUserDetails.DEFAULT_TENANT_ID, ResourceScope.USER, clazz);
    }

    public static <E extends ResourceEntity> E newResourceEntity(String userId, String tenantId, Class<E> clazz) {
        return newResourceEntity(userId, tenantId, ResourceScope.PRIVATE, clazz);
    }

    public static <E extends ResourceEntity> E newResourceEntity(String userId, ResourceScope scope, Class<E> clazz) {
        return newResourceEntity(userId, EulerUserDetails.DEFAULT_TENANT_ID, scope, clazz);
    }

    public static <E extends ResourceEntity> E newResourceEntity(String userId, String tenantId, ResourceScope scope, Class<E> clazz) {
        try {
            E resourceEntity = clazz.getDeclaredConstructor().newInstance();
            resourceEntity.setUserId(userId);
            resourceEntity.setTenantId(tenantId);
            resourceEntity.setResourceScope(scope);
            return resourceEntity;
        } catch (Exception e) {
            throw ExceptionUtils.<RuntimeException>rethrow(e);
        }
    }
}
