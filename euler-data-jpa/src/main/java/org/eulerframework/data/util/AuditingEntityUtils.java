/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.data.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.model.AbstractAuditingModel;
import org.eulerframework.model.AbstractPrincipalAuditingModel;
import org.eulerframework.data.entity.AuditingEntity;
import org.eulerframework.data.entity.PrincipalAuditingEntity;
import org.eulerframework.data.entity.ResourceEntity;

public class AuditingEntityUtils {
    public static void updateAuditingEntity(AbstractAuditingModel model, AuditingEntity entity) {
        entity.setCreatedDate(model.getCreatedDate());
        entity.setModifiedDate(model.getLastModifiedDate());
    }

    public static void updatePrincipalAuditingEntity(AbstractPrincipalAuditingModel model, PrincipalAuditingEntity entity) {
        updateAuditingEntity(model, entity);
        entity.setCreatedBy(model.getCreatedBy());
        entity.setModifiedBy(model.getLastModifiedBy());
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
