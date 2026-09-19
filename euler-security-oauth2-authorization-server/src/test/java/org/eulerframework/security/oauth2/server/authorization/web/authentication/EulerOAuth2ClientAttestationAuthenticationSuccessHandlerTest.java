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
import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.GeneratedChallenge;
import org.eulerframework.security.authentication.InMemoryNonceService;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.apple.AppleAppAttestValidationService;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerClientAttestationProof;
import org.eulerframework.security.oauth2.core.EulerOAuth2ClientAttestationType;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2HeaderNames;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationAuthenticationToken;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationVerifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link EulerOAuth2ClientAttestationAuthenticationSuccessHandler}: it applies an
 * attestation as an additional signal on top of a traditional client authentication, republishing
 * the client with the verified registration as its credentials, and otherwise replicates the default
 * handler's publication of the client to the {@code SecurityContext}.
 */
class EulerOAuth2ClientAttestationAuthenticationSuccessHandlerTest {

    private static final String CLIENT_ID = "client-1";
    private static final String KID_VALUE = "kid-1";
    private static final String CHALLENGE_VALUE = "challenge-1";
    private static final String ASSERTION_VALUE = "assertion-1";
    private static final String TRADITIONAL_CREDENTIAL = "the-client-secret";

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void publishesTheClientUnchangedWhenTheRequestCarriesNoAttestation() {
        RecordingValidationService validationService = new RecordingValidationService();
        EulerOAuth2ClientAttestationAuthenticationSuccessHandler handler = handler(validationService);
        OAuth2ClientAuthenticationToken result = traditionalResult(secretClient());

        handler.onAuthenticationSuccess(request(Map.of(), Map.of()), null, result);

        assertSame(result, SecurityContextHolder.getContext().getAuthentication());
        assertEquals(0, validationService.assertionCalls.get());
    }

    @Test
    void skipsEnhancementForTheBasicAttestPath() {
        // method == attest_jwt_client_auth: the provider already verified the attestation and carried
        // the registration; the handler must not verify again (which would double-consume the challenge).
        RecordingValidationService validationService = new RecordingValidationService();
        EulerOAuth2ClientAttestationAuthenticationSuccessHandler handler = handler(validationService);
        AppAttestAttestationRegistration registration = registration(KID_VALUE, CLIENT_ID);
        OAuth2ClientAuthenticationToken result = new OAuth2ClientAuthenticationToken(
                attestClient(), EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH, registration);

        handler.onAuthenticationSuccess(request(appleHeaders(), Map.of()), null, result);

        assertSame(result, SecurityContextHolder.getContext().getAuthentication());
        assertEquals(0, validationService.assertionCalls.get(), "the basic path is not re-verified here");
    }

    @Test
    void verifiesTheAttestationAndRepublishesTheClientCarryingTheRegistration() {
        RecordingValidationService validationService = new RecordingValidationService();
        EulerOAuth2ClientAttestationAuthenticationSuccessHandler handler = handler(validationService);

        handler.onAuthenticationSuccess(request(appleHeaders(), Map.of()), null, traditionalResult(secretClient()));

        EulerOAuth2ClientAttestationAuthenticationToken published =
                (EulerOAuth2ClientAttestationAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertEquals(ClientAuthenticationMethod.CLIENT_SECRET_BASIC, published.getClientAuthenticationMethod(),
                "the traditional method is preserved; the attestation is only an additional signal");
        assertEquals(CLIENT_ID, published.getRegisteredClient().getClientId());
        assertEquals(TRADITIONAL_CREDENTIAL, published.getCredentials(),
                "the credential the traditional authentication produced is preserved, not clobbered");
        assertSame(validationService.lastAssertionResult, published.getVerifiedRegistration(),
                "the verified registration rides in a dedicated slot for grant components to read");
        assertEquals(EulerClientAttestationProof.ASSERTION, published.getProof(),
                "the enhanced path resolves the proof from the same parameters it verified");
        assertEquals(1, validationService.assertionCalls.get());
    }

    @Test
    void rejectsWhenTheAttestationResolvesToADifferentClient() {
        // Section 7.6: an attestation presented alongside a traditional credential must resolve to the
        // very client that authenticated.
        RecordingValidationService validationService = new RecordingValidationService();
        validationService.boundClientId = "some-other-client";
        EulerOAuth2ClientAttestationAuthenticationSuccessHandler handler = handler(validationService);

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> handler.onAuthenticationSuccess(
                        request(appleHeaders(), Map.of()), null, traditionalResult(secretClient())));

        assertEquals(OAuth2ErrorCodes.INVALID_CLIENT, ex.getError().getErrorCode());
    }

    // ---- helpers ----

    private static EulerOAuth2ClientAttestationAuthenticationSuccessHandler handler(
            AppleAppAttestValidationService validationService) {
        EulerOAuth2ClientAttestationVerifier verifier = new EulerOAuth2ClientAttestationVerifier(
                new RecordingChallengeService(), new InMemoryNonceService());
        verifier.setAppleAppAttestValidationService(validationService);
        return new EulerOAuth2ClientAttestationAuthenticationSuccessHandler(verifier);
    }

    private static OAuth2ClientAuthenticationToken traditionalResult(RegisteredClient client) {
        return new OAuth2ClientAuthenticationToken(
                client, ClientAuthenticationMethod.CLIENT_SECRET_BASIC, TRADITIONAL_CREDENTIAL);
    }

    private static RegisteredClient secretClient() {
        return RegisteredClient.withId("id-secret")
                .clientId(CLIENT_ID)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
    }

    private static RegisteredClient attestClient() {
        return RegisteredClient.withId("id-attest")
                .clientId(CLIENT_ID)
                .clientAuthenticationMethod(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
    }

    private static Map<String, String> appleHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE,
                EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST.value());
        headers.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE, CHALLENGE_VALUE);
        headers.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID, KID_VALUE);
        headers.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, ASSERTION_VALUE);
        return headers;
    }

    private static AppAttestAttestationRegistration registration(String keyId, String clientId) {
        return new AppAttestAttestationRegistration(keyId, "ABCD1234EF", "com.example.app", clientId,
                null, null, null, null, null, null, 0);
    }

    private static HttpServletRequest request(Map<String, String> headers, Map<String, String> parameters) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getHeader" -> headers.get((String) args[0]);
                    case "getParameter" -> parameters.get((String) args[0]);
                    case "toString" -> "HttpServletRequestStub";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> primitiveDefault(method.getReturnType());
                });
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
        return null;
    }

    // ---- fakes ----

    static class RecordingChallengeService implements ChallengeService {
        @Override
        public GeneratedChallenge generateChallenge() {
            return new GeneratedChallenge(CHALLENGE_VALUE);
        }

        @Override
        public boolean consumeChallenge(String challenge) {
            return true;
        }
    }

    static class RecordingValidationService implements AppleAppAttestValidationService {

        final AtomicInteger assertionCalls = new AtomicInteger();
        volatile AppAttestAttestationRegistration lastAssertionResult;
        volatile String boundClientId = CLIENT_ID;

        @Override
        public AppAttestAttestationRegistration validateAttestation(String attestation, String challenge) {
            return registration("kid-derived", this.boundClientId);
        }

        @Override
        public AppAttestAttestationRegistration validateAssertion(String keyId, String assertion, String challenge) {
            this.assertionCalls.incrementAndGet();
            this.lastAssertionResult = registration(keyId, this.boundClientId);
            return this.lastAssertionResult;
        }
    }
}
