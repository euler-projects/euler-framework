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
package org.eulerframework.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Base mapped superclass that adds JPA-managed auditing timestamps to every
 * persistent entity.
 *
 * <p>Timestamps are captured as {@link Instant} so they carry explicit UTC
 * semantics and are immune to JVM default time-zone drift. Values are
 * populated automatically by Spring Data's {@link AuditingEntityListener}
 * on {@code @PrePersist} / {@code @PreUpdate}; application code should not
 * set them directly except when mirroring values from an external source.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditingEntity {

    /**
     * Instant at which the entity was first persisted. Populated once on
     * insert and never updated afterwards.
     */
    @CreatedDate
    @Column(name = "created_date")
    private Instant createdDate;

    /**
     * Instant of the most recent update. Refreshed on every persist and
     * merge operation by the auditing listener.
     */
    @LastModifiedDate
    @Column(name = "modified_date")
    private Instant modifiedDate;

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Instant modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
