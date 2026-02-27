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
package org.eulerframework.json.fastjson2;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.eulerframework.resource.ResourceScope;

import java.lang.reflect.Type;

/**
 * A Fastjson2 {@link ObjectWriter} for {@link ResourceScope} that serializes
 * the scope as a plain JSON integer representing its numeric level.
 *
 * <p>Serialization example:
 * <pre>{@code
 * // ResourceScope.PUBLIC (level = 600) serializes to:
 * 600
 * }</pre>
 *
 * <p>Register this writer globally before using {@code JSON.toJSONString}:
 * <pre>{@code
 * JSONFactory.getDefaultObjectWriterProvider().register(ResourceScope.class, new ResourceScopeWriter());
 * }</pre>
 *
 * @see ResourceScopeReader
 * @see ResourceScope
 */
public class ResourceScopeWriter implements ObjectWriter<ResourceScope> {

    /**
     * Writes the numeric level of the given {@link ResourceScope} as a JSON integer.
     * If {@code object} is {@code null}, a JSON {@code null} is written.
     *
     * @param jsonWriter the writer to write to
     * @param object     the {@link ResourceScope} to serialize, or {@code null}
     * @param fieldName  the field name in the parent object, may be {@code null}
     * @param fieldType  the declared type of the field, may be {@code null}
     * @param features   serialization feature flags
     */
    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
        } else {
            jsonWriter.writeInt32(((ResourceScope) object).getLevel());
        }
    }
}
