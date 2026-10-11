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

import org.eulerframework.security.authentication.otp.OneTimePasswordAuthenticationToken;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link OAuth2AuthorizationChallengeAuthenticationProvider}: minting a redirect-less
 * {@code authorization_code} bound to the PKCE {@code code_challenge}, and applying the same
 * grant-type / scope / PKCE validations as the classic authorization endpoint.
 */
class OAuth2AuthorizationChallengeAuthenticationProviderTest {

    private static final String CLIENT_ID = "client-1";
    private static final String AUTHORIZATION_URI = "https://example.com/authorize-challenge";
    private static final OAuth2TokenType CODE_TOKEN_TYPE = new OAuth2TokenType(OAuth2ParameterNames.CODE);

    private final OAuth2AuthorizationService authorizationService = new InMemoryOAuth2AuthorizationService();

    @Test
    void mintsARedirectlessAuthorizationCodeBoundToTheChallenge() {
        OAuth2AuthorizationChallengeAuthenticationProvider provider = provider(succeedingManager());

        OAuth2AuthorizationChallengeAuthenticationToken result =
                (OAuth2AuthorizationChallengeAuthenticationToken) provider.authenticate(
                        token(client(true), Set.of("openid"), "challenge-1", "S256"));

        assertTrue(result.isAuthenticated());
        String code = result.getAuthorizationCode();
        assertNotNull(code);

        OAuth2Authorization saved = this.authorizationService.findByToken(code, CODE_TOKEN_TYPE);
        assertNotNull(saved, "the authorization code must be persisted for the token endpoint to redeem");
        assertEquals(AuthorizationGrantType.AUTHORIZATION_CODE, saved.getAuthorizationGrantType());
        assertEquals(Set.of("openid"), saved.getAuthorizedScopes());

        OAuth2AuthorizationRequest authorizationRequest =
                saved.getAttribute(OAuth2AuthorizationRequest.class.getName());
        assertNotNull(authorizationRequest);
        assertNull(authorizationRequest.getRedirectUri(), "the challenge flow is redirect-less");
        assertEquals("challenge-1", authorizationRequest.getAdditionalParameters().get(PkceParameterNames.CODE_CHALLENGE));
        assertEquals("S256", authorizationRequest.getAdditionalParameters().get(PkceParameterNames.CODE_CHALLENGE_METHOD));
    }

    @Test
    void omittedScopeFallsBackToTheClientScopes() {
        OAuth2AuthorizationChallengeAuthenticationProvider provider = provider(succeedingManager());

        OAuth2AuthorizationChallengeAuthenticationToken result =
                (OAuth2AuthorizationChallengeAuthenticationToken) provider.authenticate(
                        token(client(false), Set.of(), null, null));

        OAuth2Authorization saved = this.authorizationService.findByToken(result.getAuthorizationCode(), CODE_TOKEN_TYPE);
        assertEquals(Set.of("openid", "profile"), saved.getAuthorizedScopes());
    }

    @Test
    void requireProofKeyWithoutCodeChallengeIsRejected() {
        OAuth2AuthorizationChallengeAuthenticationProvider provider = provider(succeedingManager());

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider.authenticate(token(client(true), Set.of("openid"), null, null)));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains(PkceParameterNames.CODE_CHALLENGE));
    }

    @Test
    void codeChallengeWithoutS256IsRejected() {
        OAuth2AuthorizationChallengeAuthenticationProvider provider = provider(succeedingManager());

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider.authenticate(token(client(true), Set.of("openid"), "challenge-1", "plain")));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains(PkceParameterNames.CODE_CHALLENGE_METHOD));
    }

    @Test
    void clientWithoutAuthorizationCodeGrantIsRejected() {
        RegisteredClient client = RegisteredClient.withId("id-" + UUID.randomUUID())
                .clientId(CLIENT_ID)
                .clientAuthenticationMethod(EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .build();
        OAuth2AuthorizationChallengeAuthenticationProvider provider = provider(succeedingManager());

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider.authenticate(token(client, Set.of(), null, null)));

        assertEquals(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT, ex.getError().getErrorCode());
    }

    @Test
    void disallowedScopeIsRejected() {
        OAuth2AuthorizationChallengeAuthenticationProvider provider = provider(succeedingManager());

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider.authenticate(token(client(false), Set.of("not-allowed"), null, null)));

        assertEquals(OAuth2ErrorCodes.INVALID_SCOPE, ex.getError().getErrorCode());
    }

    @Test
    void otpVerificationFailureMapsToInvalidGrant() {
        OAuth2AuthorizationChallengeAuthenticationProvider provider = provider(failingManager());

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider.authenticate(token(client(false), Set.of("openid"), null, null)));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
    }

    @Test
    void supportsTheChallengeToken() {
        assertTrue(provider(succeedingManager())
                .supports(OAuth2AuthorizationChallengeAuthenticationToken.class));
    }

    // ---- helpers ----

    private OAuth2AuthorizationChallengeAuthenticationProvider provider(AuthenticationManager authenticationManager) {
        return new OAuth2AuthorizationChallengeAuthenticationProvider(authenticationManager, this.authorizationService);
    }

    private static AuthenticationManager succeedingManager() {
        return authentication -> new UsernamePasswordAuthenticationToken(
                "user-1", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private static AuthenticationManager failingManager() {
        return authentication -> {
            throw new BadCredentialsException("otp verification failed");
        };
    }

    private static OAuth2AuthorizationChallengeAuthenticationToken token(RegisteredClient registeredClient,
                                                                         Set<String> scopes,
                                                                         String codeChallenge,
                                                                         String codeChallengeMethod) {
        return new OAuth2AuthorizationChallengeAuthenticationToken(
                new OAuth2ClientAuthenticationToken(
                        registeredClient, EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH, null),
                new OneTimePasswordAuthenticationToken("ticket-1", "123456"),
                AUTHORIZATION_URI,
                scopes,
                codeChallenge,
                codeChallengeMethod);
    }

    private static RegisteredClient client(boolean requireProofKey) {
        return RegisteredClient.withId("id-" + UUID.randomUUID())
                .clientId(CLIENT_ID)
                .clientAuthenticationMethod(EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://client.example.com/callback")
                .scope("openid")
                .scope("profile")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(requireProofKey)
                        .requireAuthorizationConsent(false)
                        .build())
                .build();
    }
}
