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
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.text.ParseException;
import java.util.Map;

/**
 * Jackson deserializer that reads JSON into a {@code Map<String, Object>} and then
 * reconstructs a {@link JWKSet} via {@link JWKSet#parse(Map)}.
 *
 * <p>Deserialization path: JSON → {@code Map<String, Object>} → {@link JWKSet#parse(Map)} → {@link JWKSet}</p>
 */
public class JWKSetDeserializer extends ValueDeserializer<JWKSet> {
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {
    };

    @Override
    public JWKSet deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonNode node = ctxt.readTree(p);

        if (node.isNull() || node.isEmpty()) {
            return null;
        }

        Map<String, Object> map = ctxt.readTreeAsValue(node,
                ctxt.getTypeFactory().constructType(MAP_TYPE_REFERENCE));

        if (map == null || map.isEmpty()) {
            return null;
        }

        try {
            return JWKSet.parse(map);
        } catch (ParseException e) {
            throw new JacksonException("Failed to parse JWKSet from JSON", e) {
            };
        }
    }
}
