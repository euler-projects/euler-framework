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
import jakarta.persistence.Convert;
import jakarta.persistence.MappedSuperclass;
import org.eulerframework.data.convert.ResourceScopeConverter;
import org.eulerframework.resource.ResourceScope;

@MappedSuperclass
public abstract class ResourceEntity extends PrincipalAuditingEntity {

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "user_id")
    private String userId;

    @Convert(converter = ResourceScopeConverter.class)
    @Column(name = "resource_scope")
    private ResourceScope resourceScope;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ResourceScope getResourceScope() {
        return resourceScope;
    }

    public void setResourceScope(ResourceScope resourceScope) {
        this.resourceScope = resourceScope;
    }
}
