package org.eulerframework.data.util;

import org.eulerframework.data.convert.ResourceScopeConverter;
import org.eulerframework.data.entity.ResourceEntity;
import org.eulerframework.model.AbstractResourceModel;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResourceModelUtils {
    public static void updateModelResourceFields(ResourceEntity entity, AbstractResourceModel model) {
        AuditingModelUtils.updateModelPrincipalAuditingFields(entity, model);
        model.setTenantId(entity.getTenantId());
        model.setUserId(entity.getUserId());
        model.setResourceScope(entity.getResourceScope());
    }

    public static void updateModelResourceFields(ResultSet rs, AbstractResourceModel model) throws SQLException {
        AuditingModelUtils.updateModelPrincipalAuditingFields(rs, model);
        model.setUserId(rs.getString("user_id"));
        model.setTenantId(rs.getString("tenant_id"));
        model.setResourceScope(ResourceScopeConverter.INSTANCE.convertToEntityAttribute(rs.getObject("resource_scope", Integer.class)));
    }
}
