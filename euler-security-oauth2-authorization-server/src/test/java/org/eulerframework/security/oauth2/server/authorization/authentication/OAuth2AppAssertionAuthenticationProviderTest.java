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

import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.AppAttestUser;
import org.eulerframework.security.core.userdetails.EulerDeviceUserDetailsService;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.core.userdetails.UserDetailsNotFoundException;
import org.eulerframework.security.oauth2.core.EulerAuthorizationGrantType;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerClientAttestationProof;
import org.eulerframework.security.provisioning.jit.JitProvisioningPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link OAuth2AppAssertionAuthenticationProvider}, focusing on when the
 * device-to-user association may be established: only a request that presented an
 * attestation may provision it, while an assertion-only renewal requires it to already
 * exist.
 */
class OAuth2AppAssertionAuthenticationProviderTest {

    private static final String CLIENT_ID = "client-1";

    private static final String KID = "kid-1";

    @BeforeEach
    void setUp() {
        AuthorizationServerContextHolder.setContext(new AuthorizationServerContext() {
            @Override
            public String getIssuer() {
                return "https://example.com";
            }

            @Override
            public AuthorizationServerSettings getAuthorizationServerSettings() {
                return AuthorizationServerSettings.builder().build();
            }
        });
    }

    @AfterEach
    void tearDown() {
        AuthorizationServerContextHolder.resetContext();
    }

    @Test
    void assertionOnlyRequestWithoutDeviceAssociationIsRejected() {
        RecordingDeviceUserDetailsService userDetailsService = new RecordingDeviceUserDetailsService(false);
        OAuth2AppAssertionAuthenticationProvider provider = provider(userDetailsService);

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider.authenticate(token(EulerClientAttestationProof.ASSERTION)));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
        assertEquals(0, userDetailsService.createCalls.get(),
                "an assertion-only request must never establish the device-to-user association");
    }

    @Test
    void clientWithoutAVerifiedAttestationIsRejected() {
        RecordingDeviceUserDetailsService userDetailsService = new RecordingDeviceUserDetailsService(false);
        OAuth2AppAssertionAuthenticationProvider provider = provider(userDetailsService);

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider.authenticate(tokenWithoutAttestation()));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
        assertEquals(0, userDetailsService.createCalls.get(),
                "without a verified attestation there is no device to associate a user with");
    }

    @Test
    void attestationRequestProvisionsAnAnonymousUserAndIssuesAToken() {
        RecordingDeviceUserDetailsService userDetailsService = new RecordingDeviceUserDetailsService(false);
        OAuth2AppAssertionAuthenticationProvider provider = provider(userDetailsService);

        Authentication result = provider.authenticate(token(EulerClientAttestationProof.ATTESTATION));

        assertEquals(1, userDetailsService.createCalls.get(),
                "the compatibility path provisions an anonymous user for an attestation request");
        assertEquals(0, userDetailsService.bindCalls.get());
        assertIssuedToken(result);
    }

    @Test
    void assertionOnlyRequestWithExistingAssociationRenewsTheToken() {
        RecordingDeviceUserDetailsService userDetailsService = new RecordingDeviceUserDetailsService(true);
        OAuth2AppAssertionAuthenticationProvider provider = provider(userDetailsService);

        Authentication result = provider.authenticate(token(EulerClientAttestationProof.ASSERTION));

        assertEquals(0, userDetailsService.createCalls.get(),
                "an existing association is read, never re-created");
        assertIssuedToken(result);
    }

    // ---- helpers ----

    /**
     * Assert a token was issued. {@code OAuth2AccessTokenAuthenticationToken} is deliberately
     * not marked authenticated: it feeds the token endpoint response rather than the
     * {@code SecurityContext}.
     */
    private static void assertIssuedToken(Authentication result) {
        assertInstanceOf(OAuth2AccessTokenAuthenticationToken.class, result);
        assertNotNull(((OAuth2AccessTokenAuthenticationToken) result).getAccessToken());
    }

    private static OAuth2AppAssertionAuthenticationProvider provider(EulerDeviceUserDetailsService userDetailsService) {
        return new OAuth2AppAssertionAuthenticationProvider(userDetailsService,
                new InMemoryOAuth2AuthorizationService(),
                new FakeTokenGenerator(),
                JitProvisioningPolicy.enabled(List.of("ROLE_USER")));
    }

    private static OAuth2AppAssertionAuthenticationToken token(EulerClientAttestationProof proof) {
        return new OAuth2AppAssertionAuthenticationToken(
                new EulerOAuth2ClientAttestationAuthenticationToken(registeredClient(),
                        EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH, null, registration(), proof),
                null, Map.of());
    }

    /**
     * A client that authenticated without an attestation: the grant token's principal is the plain
     * client authentication, which carries no verified registration.
     */
    private static OAuth2AppAssertionAuthenticationToken tokenWithoutAttestation() {
        return new OAuth2AppAssertionAuthenticationToken(
                new OAuth2ClientAuthenticationToken(registeredClient(),
                        EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH, registration()),
                null, Map.of());
    }

    private static RegisteredClient registeredClient() {
        return RegisteredClient.withId("id-1")
                .clientId(CLIENT_ID)
                .clientAuthenticationMethod(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH)
                .authorizationGrantType(EulerAuthorizationGrantType.APP_ASSERTION)
                .build();
    }

    private static AppAttestAttestationRegistration registration() {
        return new AppAttestAttestationRegistration(KID, "ABCD1234EF", "com.example.app", CLIENT_ID,
                null, null, null, null, null, null, 0);
    }

    private static EulerUserDetails user() {
        return new EulerUserDetails(EulerUserDetails.DEFAULT_TENANT_ID, "user-1", "anonymous-1", "",
                List.of(new SimpleGrantedAuthority("ROLE_USER")), List.of());
    }

    // ---- fakes ----

    static class RecordingDeviceUserDetailsService implements EulerDeviceUserDetailsService {

        final AtomicInteger createCalls = new AtomicInteger();
        final AtomicInteger bindCalls = new AtomicInteger();

        private boolean associationExists;

        RecordingDeviceUserDetailsService(boolean associationExists) {
            this.associationExists = associationExists;
        }

        @Override
        public EulerUserDetails loadUserByDeviceUser(AppAttestUser appAttestUser) {
            if (!this.associationExists) {
                throw new UserDetailsNotFoundException(appAttestUser.getKeyId());
            }
            return user();
        }

        @Override
        public EulerUserDetails createUser(AppAttestUser appAttestUser, List<String> authorities) {
            this.createCalls.incrementAndGet();
            this.associationExists = true;
            return user();
        }

        @Override
        public void bindToUser(AppAttestUser appAttestUser, String userId) {
            this.bindCalls.incrementAndGet();
            this.associationExists = true;
        }
    }

    static class FakeTokenGenerator implements OAuth2TokenGenerator<OAuth2Token> {

        @Override
        public OAuth2Token generate(OAuth2TokenContext context) {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return null;
            }
            return new FixedOAuth2Token("access-token");
        }
    }

    static class FixedOAuth2Token implements OAuth2Token {

        private final String value;
        private final Instant issuedAt = Instant.now();

        FixedOAuth2Token(String value) {
            this.value = value;
        }

        @Override
        public String getTokenValue() {
            return this.value;
        }

        @Override
        public Instant getIssuedAt() {
            return this.issuedAt;
        }

        @Override
        public Instant getExpiresAt() {
            return this.issuedAt.plusSeconds(3600);
        }
    }
}
