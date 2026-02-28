package org.eulerframework.data.util;

import org.eulerframework.data.entity.ResourceEntity;
import org.eulerframework.model.AbstractResourceModel;
import org.eulerframework.resource.ResourceScope;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

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
        model.setResourceScope(Optional.ofNullable(rs.getObject("resource_scope", Integer.class)).map(ResourceScope::resolve).orElse(null));
    }
}
