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

import org.eulerframework.security.authentication.device.DeviceAttestationRegistrationAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.MissingNode;

import java.util.Collection;

/**
 * Jackson deserializer for {@link DeviceAttestationRegistrationAuthenticationToken}.
 * <p>
 * Note: The registration endpoint currently does not persist this token to the
 * {@code SecurityContext}; it returns an HTTP JSON response directly. This
 * deserializer is provided for forward-compatibility in case the token needs
 * to be serialized in the future (e.g. session persistence, event logging).
 */
public class DeviceAttestationRegistrationAuthenticationTokenDeserializer extends ValueDeserializer<DeviceAttestationRegistrationAuthenticationToken> {

    private static final TypeReference<Collection<GrantedAuthority>> GRANTED_AUTHORITY_COLLECTION = new TypeReference<>() {
    };

    @Override
    public DeviceAttestationRegistrationAuthenticationToken deserialize(tools.jackson.core.JsonParser jp, DeserializationContext ctxt) throws tools.jackson.core.JacksonException {
        JsonNode jsonNode = ctxt.readTree(jp);
        boolean authenticated = readJsonNode(jsonNode, "authenticated").asBoolean();
        JsonNode principalNode = readJsonNode(jsonNode, "principal");
        Object principal = getPrincipal(ctxt, principalNode);
        String keyId = readJsonNode(jsonNode, "keyId").asString();
        JsonNode authoritiesNode = readJsonNode(jsonNode, "authorities");
        Collection<? extends GrantedAuthority> authorities = ctxt.readTreeAsValue(authoritiesNode,
                ctxt.getTypeFactory().constructType(GRANTED_AUTHORITY_COLLECTION));
        DeviceAttestationRegistrationAuthenticationToken token;
        if (!authenticated) {
            String attestation = readJsonNode(jsonNode, "attestation").asString();
            String challengeId = readJsonNode(jsonNode, "challengeId").asString();
            token = DeviceAttestationRegistrationAuthenticationToken.unauthenticated(keyId, attestation, challengeId);
        } else {
            token = DeviceAttestationRegistrationAuthenticationToken.authenticated(principal, keyId, authorities);
        }
        JsonNode detailsNode = readJsonNode(jsonNode, "details");
        if (detailsNode.isNull() || detailsNode.isMissingNode()) {
            token.setDetails(null);
        }
        else {
            Object details = ctxt.readTreeAsValue(detailsNode, Object.class);
            token.setDetails(details);
        }
        return token;
    }

    private Object getPrincipal(DeserializationContext ctxt, JsonNode principalNode)
            throws StreamReadException, DatabindException {
        if (principalNode.isObject()) {
            return ctxt.readTreeAsValue(principalNode, Object.class);
        }
        if (principalNode.isNull() || principalNode.isMissingNode()) {
            return null;
        }
        return principalNode.asString();
    }

    private JsonNode readJsonNode(JsonNode jsonNode, String field) {
        return jsonNode.has(field) ? jsonNode.get(field) : MissingNode.getInstance();
    }
}
