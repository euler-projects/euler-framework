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
import org.eulerframework.security.authentication.otp.OneTimePasswordAuthenticationToken;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;
import org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationChallengeAuthenticationToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link OAuth2AuthorizationChallengeAuthenticationConverter#convert}: the request shape of
 * the authorization challenge endpoint, reading the already authenticated client off the
 * {@code SecurityContext}. Verifying the OTP and minting the code belong to the provider.
 */
class OAuth2AuthorizationChallengeAuthenticationConverterTest {

    private static final String CLIENT_ID = "client-1";
    private static final String AUTHORIZATION_URI = "https://example.com/authorize-challenge";

    private final OAuth2AuthorizationChallengeAuthenticationConverter converter =
            new OAuth2AuthorizationChallengeAuthenticationConverter();

    @BeforeEach
    void setUp() {
        // A preceding client authentication filter leaves the authenticated client on the context.
        SecurityContextHolder.getContext().setAuthentication(new OAuth2ClientAuthenticationToken(
                client(), EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH, null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void completeRequestBuildsAnUnauthenticatedToken() {
        OAuth2AuthorizationChallengeAuthenticationToken token =
                (OAuth2AuthorizationChallengeAuthenticationToken) this.converter.convert(request(Map.of(
                        OAuth2ParameterNames.RESPONSE_TYPE, "code",
                        OAuth2ParameterNames.SCOPE, "openid profile",
                        PkceParameterNames.CODE_CHALLENGE, "challenge-1",
                        PkceParameterNames.CODE_CHALLENGE_METHOD, "S256",
                        EulerOAuth2ParameterNames.OTP_TICKET, "ticket-1",
                        EulerOAuth2ParameterNames.OTP, "123456")));

        assertEquals(false, token.isAuthenticated());
        assertEquals(Set.of("openid", "profile"), token.getScopes());
        assertEquals("challenge-1", token.getCodeChallenge());
        assertEquals("S256", token.getCodeChallengeMethod());
        assertEquals(AUTHORIZATION_URI, token.getAuthorizationUri());
        assertNull(token.getAuthorizationCode());
        assertInstanceOf(OAuth2ClientAuthenticationToken.class, token.getPrincipal());

        OneTimePasswordAuthenticationToken otp =
                assertInstanceOf(OneTimePasswordAuthenticationToken.class, token.getOtpAuthentication());
        assertEquals("ticket-1", otp.getTicketId());
        assertEquals("123456", otp.getOtp());
    }

    @Test
    void missingClientPrincipalIsRejected() {
        SecurityContextHolder.clearContext();

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> this.converter.convert(request(Map.of(
                        OAuth2ParameterNames.RESPONSE_TYPE, "code",
                        EulerOAuth2ParameterNames.OTP_TICKET, "ticket-1",
                        EulerOAuth2ParameterNames.OTP, "123456"))));

        assertEquals(OAuth2ErrorCodes.INVALID_CLIENT, ex.getError().getErrorCode());
    }

    @Test
    void missingResponseTypeIsRejected() {
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> this.converter.convert(request(Map.of(
                        EulerOAuth2ParameterNames.OTP_TICKET, "ticket-1",
                        EulerOAuth2ParameterNames.OTP, "123456"))));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains(OAuth2ParameterNames.RESPONSE_TYPE));
    }

    @Test
    void nonCodeResponseTypeIsRejected() {
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> this.converter.convert(request(Map.of(
                        OAuth2ParameterNames.RESPONSE_TYPE, "token",
                        EulerOAuth2ParameterNames.OTP_TICKET, "ticket-1",
                        EulerOAuth2ParameterNames.OTP, "123456"))));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains(OAuth2ParameterNames.RESPONSE_TYPE));
    }

    @Test
    void mismatchedClientIdIsRejected() {
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> this.converter.convert(request(Map.of(
                        OAuth2ParameterNames.RESPONSE_TYPE, "code",
                        OAuth2ParameterNames.CLIENT_ID, "some-other-client",
                        EulerOAuth2ParameterNames.OTP_TICKET, "ticket-1",
                        EulerOAuth2ParameterNames.OTP, "123456"))));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains(OAuth2ParameterNames.CLIENT_ID));
    }

    @Test
    void matchingClientIdIsAccepted() {
        OAuth2AuthorizationChallengeAuthenticationToken token =
                (OAuth2AuthorizationChallengeAuthenticationToken) this.converter.convert(request(Map.of(
                        OAuth2ParameterNames.RESPONSE_TYPE, "code",
                        OAuth2ParameterNames.CLIENT_ID, CLIENT_ID,
                        EulerOAuth2ParameterNames.OTP_TICKET, "ticket-1",
                        EulerOAuth2ParameterNames.OTP, "123456")));

        OAuth2ClientAuthenticationToken clientPrincipal =
                (OAuth2ClientAuthenticationToken) token.getPrincipal();
        assertEquals(CLIENT_ID, clientPrincipal.getRegisteredClient().getClientId());
    }

    @Test
    void missingOtpTicketIsRejected() {
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> this.converter.convert(request(Map.of(
                        OAuth2ParameterNames.RESPONSE_TYPE, "code",
                        EulerOAuth2ParameterNames.OTP, "123456"))));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains(EulerOAuth2ParameterNames.OTP_TICKET));
    }

    @Test
    void missingOtpIsRejected() {
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> this.converter.convert(request(Map.of(
                        OAuth2ParameterNames.RESPONSE_TYPE, "code",
                        EulerOAuth2ParameterNames.OTP_TICKET, "ticket-1"))));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains(EulerOAuth2ParameterNames.OTP));
    }

    @Test
    void multipleScopeParametersAreRejected() {
        Map<String, String[]> parameters = baseParameters();
        parameters.put(OAuth2ParameterNames.SCOPE, new String[]{"openid", "profile"});

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> this.converter.convert(requestMulti(parameters)));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains(OAuth2ParameterNames.SCOPE));
    }

    // ---- helpers ----

    private static Map<String, String[]> baseParameters() {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put(OAuth2ParameterNames.RESPONSE_TYPE, new String[]{"code"});
        parameters.put(EulerOAuth2ParameterNames.OTP_TICKET, new String[]{"ticket-1"});
        parameters.put(EulerOAuth2ParameterNames.OTP, new String[]{"123456"});
        return parameters;
    }

    private static RegisteredClient client() {
        return RegisteredClient.withId("id-" + UUID.randomUUID())
                .clientId(CLIENT_ID)
                .clientAuthenticationMethod(EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://client.example.com/callback")
                .scope("openid")
                .scope("profile")
                .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                .build();
    }

    private static HttpServletRequest request(Map<String, String> singleValued) {
        Map<String, String[]> parameters = new HashMap<>();
        singleValued.forEach((key, value) -> parameters.put(key, new String[]{value}));
        return requestMulti(parameters);
    }

    private static HttpServletRequest requestMulti(Map<String, String[]> parameters) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getParameterMap" -> parameters;
                    case "getParameter" -> {
                        String[] values = parameters.get((String) args[0]);
                        yield values != null && values.length > 0 ? values[0] : null;
                    }
                    case "getQueryString" -> null;
                    case "getRequestURL" -> new StringBuffer(AUTHORIZATION_URI);
                    case "getMethod" -> "POST";
                    case "toString" -> "HttpServletRequestStub";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                });
    }
}
