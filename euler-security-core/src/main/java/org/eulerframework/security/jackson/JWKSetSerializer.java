/*
 * Copyright 2013-present the original author or authors.
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

package org.eulerframework.security.jackson;

import com.nimbusds.jose.jwk.JWKSet;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Map;

/**
 * Jackson serializer for {@link JWKSet} that delegates to {@link JWKSet#toJSONObject()}
 * and then serializes the resulting {@link Map}.
 *
 * <p>Serialization path: {@code JWKSet} → {@link JWKSet#toJSONObject()} → {@code Map<String, Object>} → JSON</p>
 */
public class JWKSetSerializer extends ValueSerializer<JWKSet> {

    @Override
    public void serialize(JWKSet value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        Map<String, Object> jsonObject = value.toJSONObject();
        gen.writePOJO(jsonObject);
    }
}
