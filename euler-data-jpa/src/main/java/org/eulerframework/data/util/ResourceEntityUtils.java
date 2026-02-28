package org.eulerframework.data.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.data.entity.ResourceEntity;
import org.eulerframework.model.AbstractResourceModel;
import org.eulerframework.resource.ResourceScope;
import org.eulerframework.security.core.userdetails.EulerUserDetails;

public class ResourceEntityUtils {
    public static void updateResourceEntity(AbstractResourceModel model, ResourceEntity entity) {
        AuditingEntityUtils.updatePrincipalAuditingEntity(model, entity);
        entity.setTenantId(model.getTenantId());
        entity.setUserId(model.getUserId());
        entity.setResourceScope(model.getResourceScope());
    }

    public static <E extends ResourceEntity> E newResourceEntity(String userId, Class<E> clazz) {
        return newResourceEntity(userId, EulerUserDetails.DEFAULT_TENANT_ID, ResourceScope.USER, clazz);
    }

    public static <E extends ResourceEntity> E newResourceEntity(String userId, String tenantId, Class<E> clazz) {
        return newResourceEntity(userId, tenantId, ResourceScope.PRIVATE, clazz);
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
