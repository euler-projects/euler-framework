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

import org.eulerframework.security.authentication.otp.OneTimePasswordAuthenticationToken;
import org.eulerframework.security.core.identity.UserIdentity;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.springframework.security.core.GrantedAuthority;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.MissingNode;

import java.util.Collection;

/**
 * Rebuilds a {@link OneTimePasswordAuthenticationToken} through its static factories, since both
 * constructors are private.
 * <p>
 * The unauthenticated form is never persisted, but is handled so that the two shapes the token
 * declares round-trip symmetrically.
 */
class OneTimePasswordAuthenticationTokenDeserializer extends ValueDeserializer<OneTimePasswordAuthenticationToken> {

    private static final TypeReference<Collection<GrantedAuthority>> GRANTED_AUTHORITY_COLLECTION = new TypeReference<>() {
    };

    @Override
    public OneTimePasswordAuthenticationToken deserialize(JsonParser jp, DeserializationContext ctxt)
            throws JacksonException {
        JsonNode jsonNode = ctxt.readTree(jp);
        boolean authenticated = readJsonNode(jsonNode, "authenticated").asBoolean();

        OneTimePasswordAuthenticationToken token;
        if (authenticated) {
            JsonNode principalNode = readJsonNode(jsonNode, "principal");
            EulerUserDetails principal = isAbsent(principalNode)
                    ? null
                    : ctxt.readTreeAsValue(principalNode, EulerUserDetails.class);
            JsonNode userIdentityNode = readJsonNode(jsonNode, "userIdentity");
            UserIdentity userIdentity = isAbsent(userIdentityNode)
                    ? null
                    : ctxt.readTreeAsValue(userIdentityNode, UserIdentity.class);
            Collection<? extends GrantedAuthority> authorities = ctxt.readTreeAsValue(
                    readJsonNode(jsonNode, "authorities"),
                    ctxt.getTypeFactory().constructType(GRANTED_AUTHORITY_COLLECTION));
            token = OneTimePasswordAuthenticationToken.authenticated(principal, userIdentity, authorities);
        } else {
            // The submitted OTP is the field "otp"; "credentials" is the getter-derived name used
            // before this deserializer existed.
            JsonNode otpNode = readJsonNode(jsonNode, "otp");
            String otp = isAbsent(otpNode) ? readJsonNode(jsonNode, "credentials").asString(null) : otpNode.asString(null);
            token = OneTimePasswordAuthenticationToken.unauthenticated(readJsonNode(jsonNode, "principal").asString(null), otp);
        }

        JsonNode detailsNode = readJsonNode(jsonNode, "details");
        token.setDetails(isAbsent(detailsNode) ? null : ctxt.readTreeAsValue(detailsNode, Object.class));
        return token;
    }

    private static boolean isAbsent(JsonNode jsonNode) {
        return jsonNode.isNull() || jsonNode.isMissingNode();
    }

    private static JsonNode readJsonNode(JsonNode jsonNode, String field) {
        return jsonNode.has(field) ? jsonNode.get(field) : MissingNode.getInstance();
    }
}
