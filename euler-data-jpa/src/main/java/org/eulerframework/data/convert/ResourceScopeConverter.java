/*
 * Copyright 2013-2026 the original author or authors.
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
package org.eulerframework.data.convert;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.eulerframework.resource.ResourceScope;

/**
 * JPA {@link ResourceScopeConverter} that maps a {@link ResourceScope} to its numeric
 * {@link ResourceScope#getLevel() visibility level} when persisting to the database, and
 * delegates to {@link ResourceScope#resolve(int)} when reading from the database.
 *
 * <p>This converter is <em>not</em> registered for auto-apply ({@code autoApply = false}) so that
 * it must be opted into explicitly via {@code @Convert(converter = ResourceScopeConverter.class)}
 * on each entity attribute.
 *
 * <p>Usage example:
 * <pre>{@code
 *   @Convert(converter = ResourceScopeConverter.class)
 *   @Column(name = "scope")
 *   private ResourceScope scope;
 * }</pre>
 */
@Converter
public class ResourceScopeConverter implements AttributeConverter<ResourceScope, Integer> {
    public final static ResourceScopeConverter INSTANCE = new ResourceScopeConverter();

    @Override
    public Integer convertToDatabaseColumn(ResourceScope attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getLevel();
    }

    @Override
    public ResourceScope convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return ResourceScope.resolve(dbData);
    }
}
