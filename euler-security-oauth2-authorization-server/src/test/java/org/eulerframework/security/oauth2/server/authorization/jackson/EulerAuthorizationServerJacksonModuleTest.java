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

package org.eulerframework.security.oauth2.server.authorization.jackson;

import org.eulerframework.security.authentication.appattest.AppAttestInstanceKeyRegistration;
import org.eulerframework.security.core.identity.UserIdentity;
import org.eulerframework.security.core.identity.UserIdentityService;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.jackson.EulerSecurityJsonMapperFactory;
import org.eulerframework.security.oauth2.server.authorization.authentication.PublicKeyAuthentication;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the serialization contract for the principal the jwt-bearer grant writes into an
 * {@code OAuth2Authorization}.
 * <p>
 * It pins the discovery along with it. The mapper is assembled in {@code euler-security-core},
 * which cannot see this module, so the only route by which these types become readable is this
 * module's {@code META-INF/services} declaration being found. A round trip that passes is therefore
 * also proof the declaration is packaged and that both halves of it were done &mdash; allowing the
 * type id, and supplying the mixin that reads it.
 */
class EulerAuthorizationServerJacksonModuleTest {

    private static final TypeReference<Map<String, Object>> ATTRIBUTE_MAP = new TypeReference<>() {
    };

    private final JsonMapper jsonMapper = EulerSecurityJsonMapperFactory.getInstance();

    /**
     * The {@code public_key} backend projects its key as a JSON <em>object</em> rather than an
     * escaped string, so the identity this grant stores carries a nested map, and the authorities
     * carry the factor the authenticator stamps on. Both have to survive the round trip: the
     * authorization store reads the whole attribute map back in one pass, so one type the
     * polymorphic validator does not admit takes the read down with it, and unrelated endpoints such
     * as OIDC UserInfo go with it.
     */
    @Test
    void roundTripsAPublicKeyAuthenticationCarryingItsJwkExtension() {
        Map<String, Object> jwk = new LinkedHashMap<>();
        jwk.put("kty", "EC");
        jwk.put("crv", "P-256");
        jwk.put("kid", "thumbprint-1");

        UserIdentity userIdentity = UserIdentity
                .withExtensions(Map.of(UserIdentityService.PROPERTY_JWK, jwk))
                .identityId("idt_2")
                .identityType(AppAttestInstanceKeyRegistration.USER_IDENTITY_TYPE)
                .subject("thumbprint-1")
                .userId("usr_1")
                .boundAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();

        List<GrantedAuthority> authorities = new ArrayList<>(userDetails().getAuthorities());
        authorities.add(FactorGrantedAuthority.fromFactor(PublicKeyAuthentication.PUBLIC_KEY_FACTOR));
        PublicKeyAuthentication token = new PublicKeyAuthentication(userDetails(), userIdentity, authorities);

        PublicKeyAuthentication read =
                assertInstanceOf(PublicKeyAuthentication.class, readPrincipal(writePrincipal(token)));

        assertTrue(read.isAuthenticated());
        assertNull(read.getCredentials(), "a signature over a challenge is not a replayable credential");
        assertEquals("usr_1", read.getPrincipal().getUserId());
        assertEquals(authorities.size(), read.getAuthorities().size());

        UserIdentity restored = read.getUserIdentity();
        assertEquals(AppAttestInstanceKeyRegistration.USER_IDENTITY_TYPE, restored.getIdentityType());
        assertEquals("thumbprint-1", restored.getSubject());
        assertEquals(jwk, restored.getExtensions().get(UserIdentityService.PROPERTY_JWK),
                "the key has to come back as the same JSON object, not as a string");
    }

    // ---- helpers ----

    private static EulerUserDetails userDetails() {
        return EulerUserDetails.builder()
                .userId("usr_1")
                .username("euler")
                .password("password")
                .authorities("user")
                .build();
    }

    private String writePrincipal(Object token) {
        return writeAttributes(Map.of(Principal.class.getName(), token));
    }

    private Object readPrincipal(String json) {
        return readAttributes(json).get(Principal.class.getName());
    }

    private String writeAttributes(Map<String, Object> attributes) {
        // A LinkedHashMap rather than the immutable map, to match the attribute map the Redis
        // authorization service actually stores.
        return this.jsonMapper.writeValueAsString(new LinkedHashMap<>(attributes));
    }

    private Map<String, Object> readAttributes(String json) {
        return this.jsonMapper.readValue(json, ATTRIBUTE_MAP);
    }
}
