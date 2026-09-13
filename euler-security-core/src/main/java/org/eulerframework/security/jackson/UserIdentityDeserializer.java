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

import org.eulerframework.security.core.identity.UserIdentity;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Rebuilds a {@link UserIdentity} through its builder, since its constructor is private.
 * <p>
 * Envelope fields absent from the JSON are skipped rather than passed as {@code null}, so a
 * prototype-shaped identity (only {@code identityType} plus extension attributes) round-trips as
 * well as a persisted one.
 */
class UserIdentityDeserializer extends ValueDeserializer<UserIdentity> {

    private static final String TYPE_ID_PROPERTY = "@class";

    @Override
    public UserIdentity deserialize(JsonParser jp, DeserializationContext ctxt) throws JacksonException {
        JsonNode jsonNode = ctxt.readTree(jp);
        UserIdentity.Builder builder = UserIdentity.builder();

        readText(jsonNode, "identityId", builder::identityId);
        readText(jsonNode, "identityType", builder::identityType);
        readText(jsonNode, "subject", builder::subject);
        readText(jsonNode, "userId", builder::userId);

        JsonNode boundAtNode = jsonNode.get("boundAt");
        if (!isAbsent(boundAtNode)) {
            builder.boundAt(ctxt.readTreeAsValue(boundAtNode, Instant.class));
        }

        JsonNode extensionsNode = jsonNode.get("extensions");
        if (extensionsNode != null && extensionsNode.isObject()) {
            for (Map.Entry<String, JsonNode> entry : extensionsNode.properties()) {
                if (!TYPE_ID_PROPERTY.equals(entry.getKey())) {
                    builder.property(entry.getKey(), ctxt.readTreeAsValue(entry.getValue(), Object.class));
                }
            }
        }

        return builder.build();
    }

    private static void readText(JsonNode jsonNode, String field, Consumer<String> setter) {
        JsonNode node = jsonNode.get(field);
        if (isAbsent(node)) {
            return;
        }
        String value = node.asString(null);
        if (StringUtils.hasText(value)) {
            setter.accept(value);
        }
    }

    private static boolean isAbsent(JsonNode jsonNode) {
        return jsonNode == null || jsonNode.isNull() || jsonNode.isMissingNode();
    }
}
