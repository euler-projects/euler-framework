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
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.security.Principal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the serialization contract for the values an {@code OAuth2Authorization} carries under its
 * {@code java.security.Principal} attribute.
 * <p>
 * Every grant provider writes the end-user {@code Authentication} there, and the authorization
 * store reads the whole attribute map back in one pass. A single type missing from the
 * {@link tools.jackson.databind.jsontype.PolymorphicTypeValidator} therefore fails the read
 * outright, taking unrelated endpoints such as OIDC UserInfo down with it.
 */
class EulerSecurityJacksonModuleTest {

    private static final TypeReference<Map<String, Object>> ATTRIBUTE_MAP = new TypeReference<>() {
    };

    /**
     * The attribute map as written before {@code OneTimePasswordAuthenticationToken} was registered:
     * getter-derived, so it also carries {@code name} and {@code credentials}. Authorizations issued
     * by an OTP grant are still stored in this shape, so it has to keep reading.
     */
    private static final String LEGACY_OTP_ATTRIBUTES = """
            {"@class":"java.util.LinkedHashMap","java.security.Principal":{"@class":"org.eulerframework.security.authentication.otp.OneTimePasswordAuthenticationToken","authenticated":true,"authorities":["java.util.Collections$UnmodifiableRandomAccessList",[{"@class":"org.springframework.security.core.authority.SimpleGrantedAuthority","authority":"user"}]],"credentials":null,"details":null,"name":"euler","otp":null,"principal":{"@class":"org.eulerframework.security.core.userdetails.EulerUserDetails","accountNonExpired":true,"accountNonLocked":true,"authorities":["java.util.Collections$UnmodifiableSet",[{"@class":"org.springframework.security.core.authority.SimpleGrantedAuthority","authority":"user"}]],"credentialsNonExpired":true,"enabled":true,"password":"password","tags":["java.util.ImmutableCollections$ListN",[]],"tenantId":"default","userId":"usr_1","username":"euler"},"userIdentity":{"@class":"org.eulerframework.security.core.identity.UserIdentity","boundAt":1767225600.000000000,"extensions":{"@class":"java.util.Collections$UnmodifiableMap"},"identityId":"idt_1","identityType":"phone","subject":"subject-1","userId":"usr_1"}}}""";

    private final JsonMapper jsonMapper = EulerSecurityJsonMapperFactory.getInstance();

    @Test
    void roundTripsAnAuthenticatedOtpTokenCarriedAsTheAuthorizationPrincipal() {
        OneTimePasswordAuthenticationToken token = OneTimePasswordAuthenticationToken.authenticated(
                userDetails(), userIdentity(), userDetails().getAuthorities());

        OneTimePasswordAuthenticationToken read = readPrincipal(writePrincipal(token));

        assertTrue(read.isAuthenticated());
        assertNull(read.getOtp(), "the authenticated form carries no OTP");

        EulerUserDetails principal = assertInstanceOf(EulerUserDetails.class, read.getPrincipal());
        assertEquals("usr_1", principal.getUserId());
        assertEquals("euler", principal.getUsername());
        assertEquals(1, principal.getAuthorities().size());

        UserIdentity userIdentity = read.getUserIdentity();
        assertEquals("idt_1", userIdentity.getIdentityId());
        assertEquals("phone", userIdentity.getIdentityType());
        assertEquals("subject-1", userIdentity.getSubject());
        assertEquals("usr_1", userIdentity.getUserId());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), userIdentity.getBoundAt());
        assertTrue(userIdentity.getExtensions().isEmpty());
    }

    @Test
    void readsAnOtpAuthorizationStoredBeforeTheTokenWasRegistered() {
        OneTimePasswordAuthenticationToken read = readPrincipal(LEGACY_OTP_ATTRIBUTES);

        assertTrue(read.isAuthenticated());
        EulerUserDetails principal = assertInstanceOf(EulerUserDetails.class, read.getPrincipal());
        assertEquals("usr_1", principal.getUserId());
        assertEquals("phone", read.getUserIdentity().getIdentityType());
    }

    @Test
    void roundTripsUserIdentityExtensionAttributes() {
        UserIdentity userIdentity = UserIdentity.withExtensions(Map.of("nickname", "euler-nick"))
                .identityType("wechat")
                .build();

        Map<String, Object> read = readAttributes(writeAttributes(Map.of("userIdentity", userIdentity)));
        UserIdentity restored = assertInstanceOf(UserIdentity.class, read.get("userIdentity"));

        assertEquals("wechat", restored.getIdentityType());
        assertNull(restored.getIdentityId(), "a prototype-shaped identity has no envelope id");
        assertEquals(Map.of("nickname", "euler-nick"), restored.getExtensions());
    }

    @Test
    void roundTripsTheUnauthenticatedOtpToken() {
        OneTimePasswordAuthenticationToken token =
                OneTimePasswordAuthenticationToken.unauthenticated("ticket-1", "654321");

        OneTimePasswordAuthenticationToken read = readPrincipal(writePrincipal(token));

        assertFalse(read.isAuthenticated());
        assertEquals("ticket-1", read.getPrincipal());
        assertEquals("654321", read.getOtp());
        assertNull(read.getUserIdentity());
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

    private static UserIdentity userIdentity() {
        return UserIdentity.builder()
                .identityId("idt_1")
                .identityType("phone")
                .subject("subject-1")
                .userId("usr_1")
                .boundAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();
    }

    private String writePrincipal(OneTimePasswordAuthenticationToken token) {
        return writeAttributes(Map.of(Principal.class.getName(), token));
    }

    private OneTimePasswordAuthenticationToken readPrincipal(String json) {
        Object principal = readAttributes(json).get(Principal.class.getName());
        return assertInstanceOf(OneTimePasswordAuthenticationToken.class, principal);
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
