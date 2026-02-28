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
package org.eulerframework.json.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.eulerframework.resource.ResourceScope;

import java.io.IOException;

/**
 * A Gson {@link TypeAdapter} for {@link ResourceScope} that serializes and deserializes
 * the scope as a plain JSON integer representing its numeric level.
 *
 * <p>Serialization example:
 * <pre>{@code
 * // ResourceScope.PUBLIC (level = 600) serializes to:
 * 600
 * }</pre>
 *
 * <p>Register this adapter with your {@code GsonBuilder} before building the {@code Gson} instance:
 * <pre>{@code
 * Gson gson = new GsonBuilder()
 *         .registerTypeAdapter(ResourceScope.class, new ResourceScopeTypeAdapter())
 *         .create();
 * }</pre>
 *
 * <p>Deserialization delegates to {@link ResourceScope#resolve(int)}, ensuring that values
 * corresponding to the well-known standard scopes always return the canonical singleton
 * constants (e.g. {@link ResourceScope#PUBLIC}, {@link ResourceScope#PRIVATE}).
 *
 * @see ResourceScope
 */
public class ResourceScopeTypeAdapter extends TypeAdapter<ResourceScope> {

    /**
     * Writes the numeric level of {@code scope} as a JSON integer.
     * If {@code scope} is {@code null}, a JSON {@code null} is written.
     *
     * @param out   the writer to write to
     * @param scope the {@link ResourceScope} to serialize, or {@code null}
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(JsonWriter out, ResourceScope scope) throws IOException {
        if (scope == null) {
            out.nullValue();
        } else {
            out.value(scope.getLevel());
        }
    }

    /**
     * Reads a JSON integer and resolves it to a {@link ResourceScope} via
     * {@link ResourceScope#resolve(int)}. If the JSON token is {@code null},
     * {@code null} is returned.
     *
     * @param in the reader to read from
     * @return the resolved {@link ResourceScope}, or {@code null} for a JSON null token
     * @throws IOException if an I/O error occurs
     */
    @Override
    public ResourceScope read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return ResourceScope.resolve(in.nextInt());
    }
}
