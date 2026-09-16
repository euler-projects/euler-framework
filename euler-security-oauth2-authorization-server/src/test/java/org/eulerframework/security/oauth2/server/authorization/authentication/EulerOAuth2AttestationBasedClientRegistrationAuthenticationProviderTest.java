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
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistrationService;
import org.eulerframework.security.authentication.appattest.InMemoryAppAttestAttestationRegistrationService;
import org.eulerframework.security.authentication.appattest.InMemoryRegisteredAppRepository;
import org.eulerframework.security.authentication.appattest.RegisteredApp;
import org.eulerframework.security.authentication.appattest.RegisteredAppRepository;
import org.eulerframework.security.authentication.appattest.apple.AppleAppAttestValidationService;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerOAuth2ErrorCodes;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2ClientRegistration;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientRegistrationAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientRegistrationAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link EulerOAuth2AttestationBasedClientRegistrationAuthenticationProvider}, focused
 * on how it composes with Spring's {@link OAuth2ClientRegistrationAuthenticationProvider} inside
 * the shared {@code ProviderManager} of the registration endpoint.
 */
class EulerOAuth2AttestationBasedClientRegistrationAuthenticationProviderTest {

    private static final String KEY_ID = "key-id-1";
    private static final String CHALLENGE = "challenge-1";
    private static final String ASSERTION = "assertion-1";
    private static final String TEAM_ID = "ABCD1234EF";
    private static final String BUNDLE_ID = "com.example.app";

    private final FakeRegisteredClientRepository clientRepository = new FakeRegisteredClientRepository();
    private final InMemoryAppAttestAttestationRegistrationService registrationService =
            new InMemoryAppAttestAttestationRegistrationService();

    @Test
    void reportsItsOwnErrorInsteadOfFallingThroughToTheInitialAccessTokenProvider() {
        // ProviderManager records an AuthenticationException and keeps polling the remaining
        // providers. Were this token a subclass of Spring's, that provider would also claim it and
        // replace the specific failure below with a generic invalid_token, since the token carries
        // no principal. This is the composition the token type is designed to avoid.
        ProviderManager providerManager = new ProviderManager(List.of(
                provider(new StubChallengeService(false), new StubValidationService(keyRegistration(null))),
                springProvider()));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> providerManager.authenticate(token()));

        assertEquals(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION, ex.getError().getErrorCode());
    }

    @Test
    void leavesTheInitialAccessTokenPathToTheSpringProvider() {
        EulerOAuth2AttestationBasedClientRegistrationAuthenticationProvider provider =
                provider(new StubChallengeService(true), new StubValidationService(keyRegistration(null)));
        OAuth2ClientRegistrationAuthenticationProvider springProvider = springProvider();

        Class<?> appAttestToken = EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken.class;
        assertTrue(provider.supports(appAttestToken));
        assertFalse(springProvider.supports(appAttestToken),
                "Spring's provider must not claim the App Attest token, or it would mask its errors");

        Class<?> initialAccessToken = OAuth2ClientRegistrationAuthenticationToken.class;
        assertFalse(provider.supports(initialAccessToken));
        assertTrue(springProvider.supports(initialAccessToken));
    }

    @Test
    void provisionsADynamicClientAndBindsItToTheKey() {
        registrationService.saveRegistration(keyRegistration(null));
        EulerOAuth2AttestationBasedClientRegistrationAuthenticationProvider provider = provider(
                new StubChallengeService(true), new StubValidationService(keyRegistration(null)),
                new InMemoryRegisteredAppRepository(app(true)));

        Authentication result = provider.authenticate(token());

        // The endpoint filter hard-casts the result to Spring's token type to write the 201 body.
        assertEquals(OAuth2ClientRegistrationAuthenticationToken.class, result.getClass());
        OAuth2ClientRegistration response =
                ((OAuth2ClientRegistrationAuthenticationToken) result).getClientRegistration();

        RegisteredClient client = this.clientRepository.findByClientId(response.getClientId());
        assertNotNull(client, "the minted client should be saved");
        assertEquals(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH.getValue(),
                response.getTokenEndpointAuthenticationMethod());
        assertTrue(client.getClientAuthenticationMethods().contains(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH));
        assertEquals(1, client.getClientAuthenticationMethods().size());
        assertNull(client.getClientSecret(), "an App Attest client must not carry a secret");
        assertTrue(client.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN));

        assertEquals(client.getClientId(), registrationService.findByKeyId(KEY_ID).getClientId(),
                "the client_id should be bound back to the KEY registration");
    }

    @Test
    void returnsTheExistingClientWhenTheKeyIsAlreadyBound() {
        RegisteredClient bound = RegisteredClient.withId("id-1")
                .clientId("already-bound-client")
                .clientAuthenticationMethod(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .build();
        this.clientRepository.save(bound);
        registrationService.saveRegistration(keyRegistration(bound.getClientId()));

        EulerOAuth2AttestationBasedClientRegistrationAuthenticationProvider provider = provider(
                new StubChallengeService(true), new StubValidationService(keyRegistration(bound.getClientId())),
                new InMemoryRegisteredAppRepository(app(true)));

        OAuth2ClientRegistration response =
                ((OAuth2ClientRegistrationAuthenticationToken) provider.authenticate(token()))
                        .getClientRegistration();

        assertEquals(bound.getClientId(), response.getClientId(), "a retry should be idempotent");
        assertEquals(1, this.clientRepository.saved.size(), "no second client should be minted");
    }

    @Test
    void rejectsAnAppThatIsNotOAuth2Enabled() {
        registrationService.saveRegistration(keyRegistration(null));
        EulerOAuth2AttestationBasedClientRegistrationAuthenticationProvider provider = provider(
                new StubChallengeService(true), new StubValidationService(keyRegistration(null)),
                new InMemoryRegisteredAppRepository(app(false)));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider.authenticate(token()));

        assertEquals(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT, ex.getError().getErrorCode());
    }

    @Test
    void rejectsAChallengeThatCannotBeConsumed() {
        EulerOAuth2AttestationBasedClientRegistrationAuthenticationProvider provider =
                provider(new StubChallengeService(false), new StubValidationService(keyRegistration(null)));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider.authenticate(token()));

        assertEquals(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION, ex.getError().getErrorCode());
    }

    // ---- helpers ----

    private EulerOAuth2AttestationBasedClientRegistrationAuthenticationProvider provider(
            ChallengeService challengeService, AppleAppAttestValidationService validationService) {
        return provider(challengeService, validationService, new InMemoryRegisteredAppRepository());
    }

    private EulerOAuth2AttestationBasedClientRegistrationAuthenticationProvider provider(
            ChallengeService challengeService, AppleAppAttestValidationService validationService,
            RegisteredAppRepository registeredAppRepository) {
        return new EulerOAuth2AttestationBasedClientRegistrationAuthenticationProvider(
                challengeService, validationService, registeredAppRepository,
                this.registrationService, this.clientRepository);
    }

    private static OAuth2ClientRegistrationAuthenticationProvider springProvider() {
        OAuth2ClientRegistrationAuthenticationProvider provider = new OAuth2ClientRegistrationAuthenticationProvider(
                new InMemoryRegisteredClientRepository(dummyClient()), new InMemoryOAuth2AuthorizationService());
        provider.setOpenRegistrationAllowed(false);
        return provider;
    }

    private static RegisteredClient dummyClient() {
        return RegisteredClient.withId("dummy-id")
                .clientId("dummy-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
    }

    private static EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken token() {
        OAuth2ClientRegistration clientRegistration = OAuth2ClientRegistration.builder()
                .clientName(BUNDLE_ID)
                .grantType(AuthorizationGrantType.REFRESH_TOKEN.getValue())
                .build();
        return new EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken(
                clientRegistration, KEY_ID, CHALLENGE, ASSERTION);
    }

    private static AppAttestAttestationRegistration keyRegistration(String boundClientId) {
        return new AppAttestAttestationRegistration(KEY_ID, TEAM_ID, BUNDLE_ID, boundClientId,
                new byte[16], KEY_ID.getBytes(), new byte[0], new byte[0], null, null, 1L);
    }

    private static RegisteredApp app(boolean oauth2Enabled) {
        return RegisteredApp.withId("myapp")
                .teamId(TEAM_ID)
                .bundleId(BUNDLE_ID)
                .oauth2Enabled(oauth2Enabled)
                .build();
    }

    // ---- fakes ----

    static class StubChallengeService implements ChallengeService {

        private final boolean consumable;

        StubChallengeService(boolean consumable) {
            this.consumable = consumable;
        }

        @Override
        public GeneratedChallenge generateChallenge() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean consumeChallenge(String challenge) {
            return this.consumable;
        }
    }

    static class StubValidationService implements AppleAppAttestValidationService {

        private final AppAttestAttestationRegistration registration;

        StubValidationService(AppAttestAttestationRegistration registration) {
            this.registration = registration;
        }

        @Override
        public AppAttestAttestationRegistration validateAttestation(String attestation, String challenge) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AppAttestAttestationRegistration validateAssertion(String keyId, String assertion, String challenge) {
            return this.registration;
        }
    }

    static class FakeRegisteredClientRepository implements RegisteredClientRepository {

        private final Map<String, RegisteredClient> byClientId = new HashMap<>();
        private final Map<String, RegisteredClient> saved = new HashMap<>();

        @Override
        public void save(RegisteredClient client) {
            this.byClientId.put(client.getClientId(), client);
            this.saved.put(client.getClientId(), client);
        }

        @Override
        public RegisteredClient findById(String id) {
            return this.byClientId.values().stream()
                    .filter((client) -> client.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public RegisteredClient findByClientId(String clientId) {
            return this.byClientId.get(clientId);
        }
    }
}
