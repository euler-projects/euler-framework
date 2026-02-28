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

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import org.eulerframework.resource.ResourceScope;

import java.lang.reflect.Type;

/**
 * A Fastjson2 {@link ObjectReader} for {@link ResourceScope} that deserializes
 * a plain JSON integer into a {@link ResourceScope} instance.
 *
 * <p>Deserialization delegates to {@link ResourceScope#resolve(int)}, ensuring that values
 * corresponding to the well-known standard scopes always return the canonical singleton
 * constants (e.g. {@link ResourceScope#PUBLIC}, {@link ResourceScope#PRIVATE}).
 *
 * <p>Register this reader globally before using {@code JSON.parseObject}:
 * <pre>{@code
 * JSONFactory.getDefaultObjectReaderProvider().register(ResourceScope.class, new ResourceScopeReader());
 * }</pre>
 *
 * @see ResourceScopeWriter
 * @see ResourceScope
 */
public class ResourceScopeReader implements ObjectReader<ResourceScope> {

    /**
     * Reads a JSON integer and resolves it to a {@link ResourceScope} via
     * {@link ResourceScope#resolve(int)}. If the JSON token is {@code null},
     * {@code null} is returned.
     *
     * @param jsonReader the reader to read from
     * @param fieldType  the declared type of the field, may be {@code null}
     * @param fieldName  the field name in the parent object, may be {@code null}
     * @param features   deserialization feature flags
     * @return the resolved {@link ResourceScope}, or {@code null} for a JSON null token
     */
    @Override
    public ResourceScope readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        return ResourceScope.resolve(jsonReader.readInt32());
    }
}
