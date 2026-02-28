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
package org.eulerframework.security.jackson;

import org.eulerframework.security.core.EulerGrantedAuthority;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.MissingNode;

class EulerGrantedAuthorityDeserializer extends ValueDeserializer<EulerGrantedAuthority> {
    @Override
    public EulerGrantedAuthority deserialize(JsonParser jp, DeserializationContext ctxt) throws JacksonException {
        JsonNode jsonNode = ctxt.readTree(jp);
        String authority = readJsonNode(jsonNode, "authority").asString();
        String name = readJsonNode(jsonNode, "name").asString();
        String description = readJsonNode(jsonNode, "description").asString();
        return new EulerGrantedAuthority(authority, name, description);
    }

    private JsonNode readJsonNode(JsonNode jsonNode, String field) {
        return jsonNode.has(field) ? jsonNode.get(field) : MissingNode.getInstance();
    }
}
