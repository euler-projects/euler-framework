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
package org.eulerframework.data.convert;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.eulerframework.resource.ResourceScope;

/**
 * JPA {@link AttributeConverter} that maps a {@link ResourceScope} to its numeric
 * {@link ResourceScope#getVisibilityLevel() visibilityLevel} when persisting to the database, and
 * delegates to {@link ResourceScope#resolve(int)} when reading from the database.
 *
 * <p>Storing the visibility level as an integer offers two key benefits over storing a name:
 * <ul>
 *   <li><b>Stable storage:</b> the column value is not affected by changes to the constant name or
 *       declaration.</li>
 *   <li><b>Extensibility:</b> any {@link ResourceScope} instance — including application-defined
 *       ones — round-trips correctly through the database without requiring modifications to this
 *       converter.</li>
 * </ul>
 *
 * <p>This converter is <em>not</em> registered for auto-apply ({@code autoApply = false}) so that
 * it must be opted into explicitly via {@code @Convert(converter = ResourceScopeConverter.class)}
 * on each entity attribute. This avoids unexpected behaviour when an application defines its own
 * scope representation strategy.
 *
 * <p>Usage example:
 * <pre>
 *   &#64;Convert(converter = ResourceScopeConverter.class)
 *   &#64;Column(name = "scope")
 *   private ResourceScope scope;
 * </pre>
 */
@Converter
public class ResourceScopeConverter implements AttributeConverter<ResourceScope, Integer> {

    /**
     * Converts a {@link ResourceScope} to its {@link ResourceScope#getVisibilityLevel()
     * visibilityLevel} for storage in the database column.
     *
     * @param attribute the scope to convert; may be {@code null}
     * @return the visibility level integer, or {@code null} if {@code attribute} is {@code null}
     */
    @Override
    public Integer convertToDatabaseColumn(ResourceScope attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getVisibilityLevel();
    }

    /**
     * Converts a visibility-level integer read from the database back to a {@link ResourceScope}
     * by delegating to {@link ResourceScope#resolve(int)}.
     *
     * <p>The singleton constant is returned for any standard visibility level; a new
     * {@link ResourceScope} instance is constructed for application-defined levels.
     *
     * @param dbData the integer value stored in the database column; may be {@code null}
     * @return the resolved {@link ResourceScope} instance, or {@code null} if {@code dbData} is
     *         {@code null}
     */
    @Override
    public ResourceScope convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return ResourceScope.resolve(dbData);
    }
}
