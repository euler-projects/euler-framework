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

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.security.oauth2.core.EulerOAuth2ClientAttestationType;
import org.eulerframework.security.oauth2.core.EulerOAuth2ErrorCodes;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the credential gate in
 * {@link EulerOAuth2AttestationBasedClientRegistrationAuthenticationConverter}.
 * <p>
 * The endpoint is {@code permitAll} so that assertion-carrying requests can reach it, which moves
 * enforcement here. These tests cover the three outcomes that gate decides between, and that the
 * request body is not read until a credential has been established.
 */
class EulerOAuth2AttestationBasedClientRegistrationAuthenticationConverterTest {

    private static final String TYPE = EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_TYPE;
    private static final String KID = EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_KID;
    private static final String CHALLENGE = EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE;
    private static final String ASSERTION = EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_ASSERTION;

    private static final String APPLE_APP_ATTEST = EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST.value();
    private static final String KID_VALUE = "key-id-1";
    private static final String CHALLENGE_VALUE = "challenge-1";
    private static final String ASSERTION_VALUE = "assertion-1";

    private final EulerOAuth2AttestationBasedClientRegistrationAuthenticationConverter converter =
            new EulerOAuth2AttestationBasedClientRegistrationAuthenticationConverter();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsARequestCarryingNeitherCredential() {
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> this.converter.convert(request(Map.of(), "{\"client_name\":\"n\"}")));

        assertEquals(OAuth2ErrorCodes.INVALID_TOKEN, ex.getError().getErrorCode());
    }

    @Test
    void rejectsABearerTokenThatIsNotAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt()));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> this.converter.convert(request(Map.of(), "{\"client_name\":\"n\"}")));

        assertEquals(OAuth2ErrorCodes.INVALID_TOKEN, ex.getError().getErrorCode());
    }

    @Test
    void fallsThroughToTheInitialAccessTokenPathWhenTheBearerTokenIsAuthenticated() {
        authenticateInitialAccessToken();

        // Null hands the request to Spring's converter, which owns the initial access token path.
        assertNull(this.converter.convert(request(Map.of(), "{\"client_name\":\"n\"}")));
    }

    @Test
    void prefersAppAttestOverAnAuthenticatedBearerToken() {
        authenticateInitialAccessToken();

        // Only the type header is present, so the App Attest branch is taken and reports the
        // missing kid rather than falling through to the initial access token path.
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> this.converter.convert(request(Map.of(TYPE, APPLE_APP_ATTEST), "{}")));

        assertEquals(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains(KID));
    }

    @Test
    void rejectsAnUnsupportedClientAttestationType() {
        Map<String, String> headers = appAttestHeaders();
        headers.put(TYPE, "unknown_type");

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> this.converter.convert(request(headers, "{}")));

        // EulerOAuth2ClientAttestationType.parse owns this message and names the offending value,
        // which is more useful to the caller than the header name would be.
        assertEquals(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains("unknown_type"));
    }

    @Test
    void reportsEachMissingAppAttestHeader() {
        for (String missing : List.of(TYPE, KID, CHALLENGE, ASSERTION)) {
            Map<String, String> headers = appAttestHeaders();
            headers.remove(missing);

            OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                    () -> this.converter.convert(request(headers, "{}")));

            assertEquals(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION, ex.getError().getErrorCode(),
                    () -> "unexpected error code when " + missing + " is absent");
            assertTrue(ex.getError().getDescription().contains(missing),
                    () -> "description should name " + missing + " but was: " + ex.getError().getDescription());
        }
    }

    @Test
    void doesNotReadTheBodyUntilTheAppAttestHeadersAreComplete() {
        // A malformed body would be reported as invalid_request; the missing assertion is reported
        // instead, which shows the structural checks run before the body is parsed.
        Map<String, String> headers = appAttestHeaders();
        headers.remove(ASSERTION);

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> this.converter.convert(request(headers, "not json")));

        assertEquals(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION, ex.getError().getErrorCode());
    }

    @Test
    void rejectsAnUnparseableBodyOnceTheHeadersAreComplete() {
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> this.converter.convert(request(appAttestHeaders(), "not json")));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
    }

    @Test
    void convertsACompleteAppAttestRequest() {
        var authentication = this.converter.convert(
                request(appAttestHeaders(), "{\"client_name\":\"App Attest Client\"}"));

        assertEquals(EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken.class,
                authentication.getClass());
        EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken token =
                (EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken) authentication;
        assertEquals(KID_VALUE, token.getKeyId());
        assertEquals(CHALLENGE_VALUE, token.getChallenge());
        assertEquals(ASSERTION_VALUE, token.getAssertion());
        assertEquals("App Attest Client", token.getClientRegistration().getClientName());
    }

    // ---- helpers ----

    private static void authenticateInitialAccessToken() {
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(jwt(), Collections.emptyList()));
    }

    private static Jwt jwt() {
        return new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "RS256"), Map.of("sub", "meta-client"));
    }

    private static Map<String, String> appAttestHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(TYPE, APPLE_APP_ATTEST);
        headers.put(KID, KID_VALUE);
        headers.put(CHALLENGE, CHALLENGE_VALUE);
        headers.put(ASSERTION, ASSERTION_VALUE);
        return headers;
    }

    private static HttpServletRequest request(Map<String, String> headers, String body) {
        Map<String, String> allHeaders = new LinkedHashMap<>(headers);
        allHeaders.put("Content-Type", "application/json");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getHeader" -> allHeaders.get((String) args[0]);
                    case "getHeaderNames" -> Collections.enumeration(allHeaders.keySet());
                    case "getHeaders" -> {
                        String value = allHeaders.get((String) args[0]);
                        yield value == null ? Collections.emptyEnumeration()
                                : Collections.enumeration(List.of(value));
                    }
                    case "getContentType" -> "application/json";
                    case "getCharacterEncoding" -> StandardCharsets.UTF_8.name();
                    case "getContentLength" -> bytes.length;
                    case "getContentLengthLong" -> (long) bytes.length;
                    case "getMethod" -> "POST";
                    case "getScheme" -> "http";
                    case "getServerName" -> "localhost";
                    case "getServerPort" -> 80;
                    case "getLocalPort" -> 80;
                    case "getRemoteAddr", "getRemoteHost" -> "127.0.0.1";
                    case "getRemotePort" -> 1234;
                    case "getRequestURI" -> "/oauth2/register";
                    case "getRequestURL" -> new StringBuffer("http://localhost/oauth2/register");
                    case "getQueryString" -> null;
                    case "getParameterMap" -> Map.of();
                    case "getAttributeNames" -> Collections.emptyEnumeration();
                    case "getInputStream" -> servletInputStream(bytes);
                    case "toString" -> "HttpServletRequestStub";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> primitiveDefault(method.getReturnType());
                });
    }

    private static ServletInputStream servletInputStream(byte[] bytes) {
        ByteArrayInputStream delegate = new ByteArrayInputStream(bytes);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return delegate.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() {
                return delegate.read();
            }
        };
    }

    private static Object primitiveDefault(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        return null;
    }
}
