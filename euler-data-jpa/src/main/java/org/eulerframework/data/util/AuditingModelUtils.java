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
import org.eulerframework.model.AbstractResourceModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

/**
 * Helpers that copy auditing fields from a persistent entity or a raw
 * {@link ResultSet} back into a service-layer model.
 *
 * <p>Entities store timestamps as {@link java.time.Instant}; models still
 * expose them as {@link Date}. These helpers perform the null-safe
 * conversion so that callers do not need to duplicate the boilerplate at
 * every mapping site.
 */
public class AuditingModelUtils {

    /**
     * Copies the auditing timestamps from the entity to the model,
     * converting {@link java.time.Instant} to {@link Date}. {@code null}
     * values are propagated as-is.
     */
    public static void updateModelAuditingFields(AuditingEntity entity, AbstractAuditingModel model) {
        model.setCreatedDate(Optional.ofNullable(entity.getCreatedDate()).map(Date::from).orElse(null));
        model.setLastModifiedDate(Optional.ofNullable(entity.getModifiedDate()).map(Date::from).orElse(null));
    }

    /**
     * Extends {@link #updateModelAuditingFields(AuditingEntity, AbstractAuditingModel)}
     * with the {@code createdBy} and {@code lastModifiedBy} principal fields.
     */
    public static void updateModelPrincipalAuditingFields(PrincipalAuditingEntity entity, AbstractPrincipalAuditingModel model) {
        updateModelAuditingFields(entity, model);
        model.setCreatedBy(entity.getCreatedBy());
        model.setLastModifiedBy(entity.getModifiedBy());
    }

    /**
     * Reads the auditing timestamps from a raw {@link ResultSet} into the
     * model. Intended for JDBC-based readers that bypass the JPA layer.
     */
    public static void updateModelAuditingFields(ResultSet rs, AbstractResourceModel model) throws SQLException {
        model.setCreatedDate(rs.getDate("created_date"));
        model.setLastModifiedDate(rs.getDate("modified_date"));
    }

    /**
     * Extends {@link #updateModelAuditingFields(ResultSet, AbstractResourceModel)}
     * with the {@code createdBy} and {@code lastModifiedBy} principal fields.
     */
    public static void updateModelPrincipalAuditingFields(ResultSet rs, AbstractResourceModel model) throws SQLException {
        updateModelAuditingFields(rs, model);
        model.setCreatedBy(rs.getString("created_by"));
        model.setLastModifiedBy(rs.getString("modified_by"));
    }
}
