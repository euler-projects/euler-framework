package org.eulerframework.data.util;

import org.eulerframework.core.model.AbstractAuditingModel;
import org.eulerframework.core.model.AbstractPrincipalAuditingModel;
import org.eulerframework.core.model.AbstractResourceModel;
import org.eulerframework.data.entity.AuditingEntity;
import org.eulerframework.data.entity.PrincipalAuditingEntity;
import org.eulerframework.data.entity.ResourceEntity;

public class AuditingModelUtils {
    public static void updateModelAuditingFields(AuditingEntity entity, AbstractAuditingModel model) {
        model.setCreatedDate(entity.getCreatedDate());
        model.setLastModifiedDate(entity.getModifiedDate());
    }

    public static void updateModelPrincipalAuditingFields(PrincipalAuditingEntity entity, AbstractPrincipalAuditingModel model) {
        updateModelAuditingFields(entity, model);
        model.setCreatedBy(entity.getCreatedBy());
        model.setLastModifiedBy(entity.getModifiedBy());
    }

    public static void updateModelResourceFields(ResourceEntity entity, AbstractResourceModel model) {
        updateModelPrincipalAuditingFields(entity, model);
        model.setTenantId(entity.getTenantId());
    }
}
