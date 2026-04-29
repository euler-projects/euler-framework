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

import org.eulerframework.model.AbstractAuditingModel;
import org.eulerframework.model.AbstractPrincipalAuditingModel;
import org.eulerframework.data.entity.AuditingEntity;
import org.eulerframework.data.entity.PrincipalAuditingEntity;

import java.util.Date;
import java.util.Optional;

/**
 * Helpers that copy auditing fields from a model (service-layer view) onto
 * a persistent entity before it is handed to the JPA layer.
 *
 * <p>Entities expose their timestamps as {@link java.time.Instant}, while
 * legacy models still use {@link Date}. These helpers perform the
 * null-safe conversion so that callers do not need to duplicate the
 * boilerplate at every mapping site.
 */
public class AuditingEntityUtils {

    /**
     * Copies {@code createdDate} and {@code lastModifiedDate} from the
     * model onto the entity, converting {@link Date} to {@link java.time.Instant}.
     * {@code null} values are propagated as-is.
     */
    public static void updateAuditingEntity(AbstractAuditingModel model, AuditingEntity entity) {
        entity.setCreatedDate(Optional.ofNullable(model.getCreatedDate()).map(Date::toInstant).orElse(null));
        entity.setModifiedDate(Optional.ofNullable(model.getLastModifiedDate()).map(Date::toInstant).orElse(null));
    }

    /**
     * Extends {@link #updateAuditingEntity} with the {@code createdBy} and
     * {@code lastModifiedBy} principal fields.
     */
    public static void updatePrincipalAuditingEntity(AbstractPrincipalAuditingModel model, PrincipalAuditingEntity entity) {
        updateAuditingEntity(model, entity);
        entity.setCreatedBy(model.getCreatedBy());
        entity.setModifiedBy(model.getLastModifiedBy());
    }

}
