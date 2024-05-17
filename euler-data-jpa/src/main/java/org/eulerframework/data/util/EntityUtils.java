package org.eulerframework.data.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.core.model.AbstractResourceModel;
import org.eulerframework.data.entity.AuditingEntity;
import org.eulerframework.data.entity.ResourceEntity;

import java.lang.reflect.InvocationTargetException;

public class EntityUtils {
    public static void updateModelAuditingFields(AbstractResourceModel model, AuditingEntity entity) {
        model.setCreatedBy(entity.getCreatedBy());
        model.setLastModifiedBy(entity.getModifiedBy());
        model.setCreatedDate(entity.getCreatedDate());
        model.setLastModifiedDate(entity.getModifiedDate());
    }

    public static void updateModelResourceFields(AbstractResourceModel model, ResourceEntity entity) {
        updateModelAuditingFields(model, entity);
        model.setTenantId(entity.getTenantId());
    }

    public static <E extends ResourceEntity> E newResourceEntity(String tenantId, Class<E> clazz) {
        try {
            E resourceEntity = clazz.getDeclaredConstructor().newInstance();
            resourceEntity.setTenantId(tenantId);
            return resourceEntity;
        } catch (Exception e) {
            throw ExceptionUtils.<RuntimeException>rethrow(e);
        }
    }
}
