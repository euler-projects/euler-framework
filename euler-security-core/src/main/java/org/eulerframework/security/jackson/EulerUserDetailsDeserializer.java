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

package org.eulerframework.security.jackson;

import org.eulerframework.security.core.EulerGrantedAuthority;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.MissingNode;

import java.util.Set;


class EulerUserDetailsDeserializer extends ValueDeserializer<EulerUserDetails> {

    private static final TypeReference<Set<EulerGrantedAuthority>> GRANTED_AUTHORITY_SET = new TypeReference<>() {
    };

    /**
     * This method will create {@link User} object. It will ensure successful object
     * creation even if password key is null in serialized json, because credentials may
     * be removed from the {@link User} by invoking {@link User#eraseCredentials()}. In
     * that case there won't be any password key in serialized json.
     *
     * @param jp   the JsonParser
     * @param ctxt the DeserializationContext
     * @return the user
     * @throws JacksonException if an error during JSON processing occurs
     */
    @Override
    public EulerUserDetails deserialize(JsonParser jp, DeserializationContext ctxt) throws JacksonException {
        JsonNode jsonNode = ctxt.readTree(jp);
        JsonNode authoritiesNode = readJsonNode(jsonNode, "authorities");
        Set<? extends GrantedAuthority> authorities = ctxt.readTreeAsValue(authoritiesNode,
                ctxt.getTypeFactory().constructType(GRANTED_AUTHORITY_SET));
        JsonNode passwordNode = readJsonNode(jsonNode, "password");
        String tenantId = readJsonNode(jsonNode, "tenantId").asString(EulerUserDetails.DEFAULT_TENANT_ID);
        String userId = readJsonNode(jsonNode, "userId").asString();
        String username = readJsonNode(jsonNode, "username").asString();
        String password = passwordNode.asString("");
        boolean enabled = readJsonNode(jsonNode, "enabled").asBoolean();
        boolean accountNonExpired = readJsonNode(jsonNode, "accountNonExpired").asBoolean();
        boolean credentialsNonExpired = readJsonNode(jsonNode, "credentialsNonExpired").asBoolean();
        boolean accountNonLocked = readJsonNode(jsonNode, "accountNonLocked").asBoolean();
        EulerUserDetails result = new EulerUserDetails(tenantId, userId, username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        if (passwordNode.asString(null) == null) {
            result.eraseCredentials();
        }
        return result;
    }

    private JsonNode readJsonNode(JsonNode jsonNode, String field) {
        return jsonNode.has(field) ? jsonNode.get(field) : MissingNode.getInstance();
    }

}
