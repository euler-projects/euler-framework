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

package org.eulerframework.security.oauth2.server.authorization.authentication;

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.GeneratedChallenge;
import org.eulerframework.security.authentication.InMemoryNonceService;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.apple.AppleAppAttestValidationService;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerClientAttestationProof;
import org.eulerframework.security.oauth2.core.EulerOAuth2ClientAttestationType;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2HeaderNames;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link EulerOAuth2ClientAttestationAuthenticationProvider}, the basic path in which the
 * attestation is the client's credential. It admits two token shapes &mdash; an
 * {@code attest_jwt_client_auth} token from the appended converter, and a {@code NONE} token carrying
 * an attestation and a {@code code_verifier} that {@code PublicClientAuthenticationProvider} declined
 * &mdash; verifies the attestation, resolves and validates the client, and enforces PKCE. What is
 * asserted here is that routing and those gates; the attestation cryptography itself is covered by
 * {@link EulerOAuth2ClientAttestationVerifierTest}, and PKCE by Spring's {@code CodeVerifierAuthenticator}.
 */
class EulerOAuth2ClientAttestationAuthenticationProviderTest {

    private static final String CLIENT_ID = "client-1";
    private static final String KID = "kid-1";
    private static final String CHALLENGE = "challenge-1";
    private static final String ASSERTION = "assertion-1";

    // ---- admission: not this provider's request ----

    @Test
    void returnsNullForATraditionalClientAuthenticationRequest() {
        EulerOAuth2ClientAttestationAuthenticationProvider provider = provider(attestClient());

        // Returning null lets the ProviderManager move on to the provider that owns client_secret_basic.
        assertNull(provider.authenticate(new OAuth2ClientAuthenticationToken(
                CLIENT_ID, ClientAuthenticationMethod.CLIENT_SECRET_BASIC, "secret", Map.of())));
    }

    @Test
    void returnsNullForAPublicClientTokenWithoutAnAttestation() {
        EulerOAuth2ClientAttestationAuthenticationProvider provider = provider(publicClient());

        // A plain PKCE request (NONE, code_verifier, no attestation) belongs to PublicClient's provider.
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PkceParameterNames.CODE_VERIFIER, "verifier-1");
        assertNull(provider.authenticate(new OAuth2ClientAuthenticationToken(
                CLIENT_ID, ClientAuthenticationMethod.NONE, null, params)));
    }

    // ---- basic path: attest_jwt_client_auth token ----

    @Test
    void authenticatesAnAttestationTokenAndCarriesTheVerifiedRegistration() {
        AppAttestAttestationRegistration[] captured = new AppAttestAttestationRegistration[1];
        RecordingValidationService validationService = new RecordingValidationService(captured);
        EulerOAuth2ClientAttestationAuthenticationProvider provider = provider(attestClient(), validationService);

        Authentication result = provider.authenticate(attestToken(appleParams()));

        assertTrue(result.isAuthenticated());
        EulerOAuth2ClientAttestationAuthenticationToken authenticated =
                (EulerOAuth2ClientAttestationAuthenticationToken) result;
        assertEquals(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH,
                authenticated.getClientAuthenticationMethod());
        assertEquals(CLIENT_ID, authenticated.getRegisteredClient().getClientId());
        assertSame(captured[0], authenticated.getVerifiedRegistration(),
                "the verified registration is handed downstream in a dedicated slot");
        assertEquals(EulerClientAttestationProof.ASSERTION, authenticated.getProof(),
                "the proof resolved from the collected parameters rides along, so a grant provider "
                        + "can tell this assertion-only request from an App instance registration");
    }

    @Test
    void rejectsWhenTheResolvedClientIsUnknown() {
        // The attestation resolves to CLIENT_ID, but only some-other-client is registered.
        EulerOAuth2ClientAttestationAuthenticationProvider provider = provider(clientWithId("some-other-client"));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider.authenticate(attestToken(appleParams())));

        assertEquals(OAuth2ErrorCodes.INVALID_CLIENT, ex.getError().getErrorCode());
    }

    @Test
    void rejectsWhenTheClientDoesNotDeclareAttestJwtClientAuth() {
        EulerOAuth2ClientAttestationAuthenticationProvider provider = provider(secretOnlyClient());

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider.authenticate(attestToken(appleParams())));

        assertEquals(OAuth2ErrorCodes.INVALID_CLIENT, ex.getError().getErrorCode());
    }

    // ---- PKCE-shaped path: NONE token declined by PublicClient ----

    @Test
    void authenticatesAPkceShapedTokenFromAnAttestOnlyClient() {
        RecordingValidationService validationService = new RecordingValidationService();
        // The client's real method is attest_jwt_client_auth (no NONE), so PublicClient's provider
        // declined this authorization_code + PKCE token and ProviderManager routed it here.
        EulerOAuth2ClientAttestationAuthenticationProvider provider = provider(attestClient(), validationService);

        Map<String, Object> params = appleParams();
        params.put(PkceParameterNames.CODE_VERIFIER, "verifier-1");
        Authentication result = provider.authenticate(new OAuth2ClientAuthenticationToken(
                CLIENT_ID, ClientAuthenticationMethod.NONE, null, params));

        assertTrue(result.isAuthenticated());
        OAuth2ClientAuthenticationToken authenticated = (OAuth2ClientAuthenticationToken) result;
        assertEquals(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH,
                authenticated.getClientAuthenticationMethod());
        assertEquals(CLIENT_ID, authenticated.getRegisteredClient().getClientId());
    }

    @Test
    void declinesAPkceShapedTokenFromAGenuinePublicClient() {
        // The client declares NONE, so this is a public client whose attestation is only an
        // additional signal (or whose PKCE failed); it must be left to surface its own outcome, and
        // the one-time challenge must not be burned here.
        RecordingValidationService validationService = new RecordingValidationService();
        EulerOAuth2ClientAttestationAuthenticationProvider provider = provider(publicClient(), validationService);

        Map<String, Object> params = appleParams();
        params.put(PkceParameterNames.CODE_VERIFIER, "verifier-1");
        assertNull(provider.authenticate(new OAuth2ClientAuthenticationToken(
                CLIENT_ID, ClientAuthenticationMethod.NONE, null, params)));
        assertEquals(0, validationService.assertionCalls.get(), "declined before verifying");
    }

    // ---- PKCE enforcement ----

    @Test
    void enforcesPkceForAnAuthorizationCodeRequest() {
        EulerOAuth2ClientAttestationAuthenticationProvider provider = provider(attestClient());

        // grant_type=authorization_code with an unknown code: CodeVerifierAuthenticator looks the
        // authorization up by code and rejects it, proving PKCE is wired into this path.
        Map<String, Object> params = appleParams();
        params.put(OAuth2ParameterNames.GRANT_TYPE, AuthorizationGrantType.AUTHORIZATION_CODE.getValue());
        params.put(OAuth2ParameterNames.CODE, "unknown-code");
        params.put(PkceParameterNames.CODE_VERIFIER, "verifier-1");

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider.authenticate(attestToken(params)));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
    }

    // ---- helpers ----

    private static Map<String, Object> appleParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE, EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST);
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE, CHALLENGE);
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, ASSERTION);
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID, KID);
        return params;
    }

    private static OAuth2ClientAuthenticationToken attestToken(Map<String, Object> params) {
        return new OAuth2ClientAuthenticationToken(
                "(attestation)", EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH, null, params);
    }

    private static EulerOAuth2ClientAttestationAuthenticationProvider provider(RegisteredClient client) {
        return provider(client, new RecordingValidationService());
    }

    private static EulerOAuth2ClientAttestationAuthenticationProvider provider(
            RegisteredClient client, AppleAppAttestValidationService validationService) {
        EulerOAuth2ClientAttestationVerifier verifier = new EulerOAuth2ClientAttestationVerifier(
                new RecordingChallengeService(), new InMemoryNonceService());
        verifier.setAppleAppAttestValidationService(validationService);
        return new EulerOAuth2ClientAttestationAuthenticationProvider(
                new FakeRegisteredClientRepository(client), verifier, new InMemoryOAuth2AuthorizationService());
    }

    private static RegisteredClient attestClient() {
        return clientWithId(CLIENT_ID);
    }

    private static RegisteredClient clientWithId(String clientId) {
        return RegisteredClient.withId("id-" + clientId)
                .clientId(clientId)
                .clientAuthenticationMethod(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri("https://example.com/callback")
                .clientSettings(org.springframework.security.oauth2.server.authorization.settings.ClientSettings
                        .builder().requireProofKey(true).build())
                .build();
    }

    private static RegisteredClient publicClient() {
        return RegisteredClient.withId("id-public")
                .clientId(CLIENT_ID)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://example.com/callback")
                .build();
    }

    private static RegisteredClient secretOnlyClient() {
        return RegisteredClient.withId("id-secret")
                .clientId(CLIENT_ID)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
    }

    private static AppAttestAttestationRegistration registration(String keyId, String clientId) {
        return new AppAttestAttestationRegistration(keyId, "ABCD1234EF", "com.example.app", clientId,
                null, null, null, null, null, null, 0);
    }

    // ---- fakes ----

    static class RecordingChallengeService implements ChallengeService {
        @Override
        public GeneratedChallenge generateChallenge() {
            return new GeneratedChallenge(CHALLENGE);
        }

        @Override
        public boolean consumeChallenge(String challenge) {
            return true;
        }
    }

    static class RecordingValidationService implements AppleAppAttestValidationService {

        final java.util.concurrent.atomic.AtomicInteger assertionCalls = new java.util.concurrent.atomic.AtomicInteger();
        private final AppAttestAttestationRegistration[] capture;

        RecordingValidationService() {
            this(null);
        }

        RecordingValidationService(AppAttestAttestationRegistration[] capture) {
            this.capture = capture;
        }

        @Override
        public AppAttestAttestationRegistration validateAttestation(String attestation, String challenge) {
            return registration("kid-derived", CLIENT_ID);
        }

        @Override
        public AppAttestAttestationRegistration validateAssertion(String keyId, String assertion, String challenge) {
            this.assertionCalls.incrementAndGet();
            AppAttestAttestationRegistration result = registration(keyId, CLIENT_ID);
            if (this.capture != null) {
                this.capture[0] = result;
            }
            return result;
        }
    }

    static class FakeRegisteredClientRepository implements RegisteredClientRepository {

        private final RegisteredClient client;

        FakeRegisteredClientRepository(RegisteredClient client) {
            this.client = client;
        }

        @Override
        public void save(RegisteredClient registeredClient) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RegisteredClient findById(String id) {
            return this.client.getId().equals(id) ? this.client : null;
        }

        @Override
        public RegisteredClient findByClientId(String clientId) {
            return this.client.getClientId().equals(clientId) ? this.client : null;
        }
    }
}
