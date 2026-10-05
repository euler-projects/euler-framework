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

package org.eulerframework.security.oauth2.server.authorization.web.authentication;

import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2JwtBearerAuthenticationToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link OAuth2JwtBearerAuthenticationConverter#convert}: claiming the standard
 * jwt-bearer grant and building its token without inspecting the assertion.
 * <p>
 * What the assertion means &mdash; which issuer it names, which key verifies it &mdash; is
 * the provider's business, so the converter is only responsible for the request shape.
 */
class OAuth2JwtBearerAuthenticationConverterTest {

    private static final String ASSERTION = "header.payload.signature";

    private final OAuth2JwtBearerAuthenticationConverter converter = new OAuth2JwtBearerAuthenticationConverter();

    @BeforeEach
    void setUp() {
        // A grant token always carries the client it was requested under, and the converter
        // takes it from where the client authentication filter left it.
        SecurityContextHolder.getContext().setAuthentication(clientPrincipal());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void claimsTheJwtBearerGrantAndCarriesTheAssertion() {
        OAuth2JwtBearerAuthenticationToken token = (OAuth2JwtBearerAuthenticationToken) this.converter
                .convert(request(Map.of(OAuth2ParameterNames.GRANT_TYPE, grantType(),
                        OAuth2ParameterNames.ASSERTION, ASSERTION)));

        assertEquals(AuthorizationGrantType.JWT_BEARER, token.getGrantType());
        assertEquals(ASSERTION, token.getAssertion());
        assertEquals(ASSERTION, token.getCredentials(), "the assertion is this grant's credential");
        assertTrue(token.getScopes().isEmpty());
    }

    @Test
    void leavesAnotherGrantToItsOwnConverter() {
        assertNull(this.converter.convert(request(Map.of(
                OAuth2ParameterNames.GRANT_TYPE, AuthorizationGrantType.CLIENT_CREDENTIALS.getValue(),
                OAuth2ParameterNames.ASSERTION, ASSERTION))));
    }

    @Test
    void rejectsAMissingAssertion() {
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> this.converter.convert(request(Map.of(
                        OAuth2ParameterNames.GRANT_TYPE, grantType()))));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains(OAuth2ParameterNames.ASSERTION));
    }

    @Test
    void keepsTheSignedCredentialOutOfAdditionalParameters() {
        OAuth2JwtBearerAuthenticationToken token = (OAuth2JwtBearerAuthenticationToken) this.converter
                .convert(request(Map.of(OAuth2ParameterNames.GRANT_TYPE, grantType(),
                        OAuth2ParameterNames.ASSERTION, ASSERTION,
                        OAuth2ParameterNames.SCOPE, "openid profile",
                        "custom", "kept")));

        // An assertion is a credential: it must not travel where audit or logging would pick
        // it up, which is the same reason the otp grant strips its one-time password.
        assertNull(token.getAdditionalParameters().get(OAuth2ParameterNames.ASSERTION));
        assertNull(token.getAdditionalParameters().get(OAuth2ParameterNames.GRANT_TYPE));
        assertNull(token.getAdditionalParameters().get(OAuth2ParameterNames.SCOPE));
        assertEquals(Set.of("openid", "profile"), token.getScopes());
        assertEquals("kept", token.getAdditionalParameters().get("custom"),
                "parameters this converter does not consume are passed through");
    }

    @Test
    void rejectsAScopeParameterSuppliedMoreThanOnce() {
        // A repeated scope is a format error rather than two sets of scopes to merge.
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> this.converter.convert(requestWithRepeatedScope()));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
    }

    @Test
    void carriesTheAuthenticatedClientAsThePrincipal() {
        OAuth2JwtBearerAuthenticationToken token = (OAuth2JwtBearerAuthenticationToken) this.converter
                .convert(request(Map.of(OAuth2ParameterNames.GRANT_TYPE, grantType(),
                        OAuth2ParameterNames.ASSERTION, ASSERTION)));

        // The provider needs the client to resolve the issuer the assertion has to name, and
        // that is exactly what the client authentication filter left in the context.
        assertInstanceOf(OAuth2ClientAuthenticationToken.class, token.getPrincipal());
    }

    // ---- helpers ----

    private static String grantType() {
        return AuthorizationGrantType.JWT_BEARER.getValue();
    }

    private static HttpServletRequest request(Map<String, String> parameters) {
        return TestHttpServletRequests.post(Map.of(), parameters);
    }

    private static Authentication clientPrincipal() {
        return new OAuth2ClientAuthenticationToken(RegisteredClient.withId("id-1")
                .clientId("client-1")
                .clientAuthenticationMethod(EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH)
                .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
                .build(),
                EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH, null);
    }

    /**
     * The shared stub holds one value per parameter, so a repeated {@code scope} is presented
     * the way the servlet API would: two entries under one name.
     */
    private static HttpServletRequest requestWithRepeatedScope() {
        Map<String, String[]> parameterMap = Map.of(
                OAuth2ParameterNames.GRANT_TYPE, new String[]{grantType()},
                OAuth2ParameterNames.ASSERTION, new String[]{ASSERTION},
                OAuth2ParameterNames.SCOPE, new String[]{"openid", "profile"});
        return (HttpServletRequest) Proxy.newProxyInstance(
                OAuth2JwtBearerAuthenticationConverterTest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getParameterMap" -> parameterMap;
                    case "getQueryString" -> null;
                    case "getMethod" -> "POST";
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }
}
