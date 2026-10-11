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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthenticationProviderUtilsAccessor;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mints an {@code authorization_code} at the Authorization Challenge Endpoint defined by
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-first-party-apps-04.txt">
 * draft-ietf-oauth-first-party-apps-04</a>, once the submitted one-time password is authenticated.
 * <p>
 * This is the redirect-less analogue of the authorization step Spring's
 * {@code OAuth2AuthorizationCodeRequestAuthenticationProvider} performs for {@code GET /oauth2/authorize}:
 * it applies the same {@code authorization_code} grant-type, scope and PKCE {@code code_challenge}
 * validations, then persists an {@link OAuth2Authorization} carrying the
 * {@link OAuth2AuthorizationRequest} (with {@code code_challenge}) exactly where the token endpoint's
 * {@code authorization_code} grant and its PKCE {@code code_verifier} check expect to find it. It is
 * minted here rather than delegated to that provider because the draft's flow has no
 * {@code redirect_uri}, and the standard provider resolves one from the client on success.
 * <p>
 * PKCE is enforced identically to the classic flow: a {@code code_challenge} must use {@code S256}, and
 * a client with {@code ClientSettings.requireProofKey(true)} must send one. The matching
 * {@code code_verifier} is validated later, at the token endpoint, by Spring's client authentication.
 *
 * @see OAuth2AuthorizationChallengeAuthenticationToken
 */
public class OAuth2AuthorizationChallengeAuthenticationProvider implements AuthenticationProvider {

    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";
    private static final String PKCE_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc7636#section-4.4.1";

    /**
     * Matches the entropy and encoding of Spring's package-private {@code OAuth2AuthorizationCodeGenerator}.
     */
    private static final StringKeyGenerator AUTHORIZATION_CODE_GENERATOR =
            new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);

    private final Logger logger = LoggerFactory.getLogger(OAuth2AuthorizationChallengeAuthenticationProvider.class);

    private final AuthenticationManager authenticationManager;
    private final OAuth2AuthorizationService authorizationService;

    public OAuth2AuthorizationChallengeAuthenticationProvider(AuthenticationManager authenticationManager,
                                                              OAuth2AuthorizationService authorizationService) {
        Assert.notNull(authenticationManager, "authenticationManager must not be null");
        Assert.notNull(authorizationService, "authorizationService must not be null");
        this.authenticationManager = authenticationManager;
        this.authorizationService = authorizationService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2AuthorizationChallengeAuthenticationToken challengeToken =
                (OAuth2AuthorizationChallengeAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal =
                OAuth2AuthenticationProviderUtilsAccessor.getAuthenticatedClientElseThrowInvalidClient(challengeToken);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

        // grant_type - the client must be allowed the authorization_code grant
        if (!registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.AUTHORIZATION_CODE)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Invalid request: authorization_code grant is not allowed for registered client '{}'",
                        registeredClient.getId());
            }
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        // scope - an omitted scope falls back to all scopes the client is registered for
        Set<String> requestedScopes = challengeToken.getScopes();
        Set<String> authorizedScopes = requestedScopes.isEmpty()
                ? new HashSet<>(registeredClient.getScopes())
                : requestedScopes;
        if (!registeredClient.getScopes().containsAll(authorizedScopes)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Invalid request: requested scope is not allowed for registered client '{}'",
                        registeredClient.getId());
            }
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_SCOPE);
        }

        // code_challenge (PKCE) - required for clients that requireProofKey, and must be S256
        String codeChallenge = challengeToken.getCodeChallenge();
        String codeChallengeMethod = challengeToken.getCodeChallengeMethod();
        if (StringUtils.hasText(codeChallenge)) {
            if (!StringUtils.hasText(codeChallengeMethod) || !"S256".equals(codeChallengeMethod)) {
                throw invalidRequest(PkceParameterNames.CODE_CHALLENGE_METHOD, PKCE_ERROR_URI);
            }
        } else if (registeredClient.getClientSettings().isRequireProofKey()) {
            throw invalidRequest(PkceParameterNames.CODE_CHALLENGE, PKCE_ERROR_URI);
        }

        // Complete the user-level one-time-password authentication through the shared AuthenticationManager.
        Authentication userPrincipal;
        try {
            userPrincipal = this.authenticationManager.authenticate(challengeToken.getOtpAuthentication());
        } catch (AuthenticationException e) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT,
                    "OTP verification failed", ERROR_URI), e);
        }
        if (userPrincipal == null || !userPrincipal.isAuthenticated()) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);
        }

        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put(OAuth2ParameterNames.RESPONSE_TYPE, OAuth2AuthorizationResponseType.CODE.getValue());
        if (StringUtils.hasText(codeChallenge)) {
            additionalParameters.put(PkceParameterNames.CODE_CHALLENGE, codeChallenge);
            additionalParameters.put(PkceParameterNames.CODE_CHALLENGE_METHOD, codeChallengeMethod);
        }

        // The endpoint is redirect-less: no redirect_uri and no state travel on the authorization request.
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(challengeToken.getAuthorizationUri())
                .clientId(registeredClient.getClientId())
                .redirectUri(null)
                .scopes(authorizedScopes)
                .state(null)
                .additionalParameters(additionalParameters)
                .build();

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(registeredClient.getTokenSettings().getAuthorizationCodeTimeToLive());
        OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(
                AUTHORIZATION_CODE_GENERATOR.generateKey(), issuedAt, expiresAt);

        OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(userPrincipal.getName())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizedScopes(authorizedScopes)
                .attribute(Principal.class.getName(), userPrincipal)
                .attribute(OAuth2AuthorizationRequest.class.getName(), authorizationRequest)
                .token(authorizationCode)
                .build();
        this.authorizationService.save(authorization);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Generated authorization code at the authorization challenge endpoint");
        }

        return new OAuth2AuthorizationChallengeAuthenticationToken(
                registeredClient, userPrincipal, authorizationCode.getTokenValue());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2AuthorizationChallengeAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private static OAuth2AuthenticationException invalidRequest(String parameterName, String errorUri) {
        return new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST,
                "OAuth 2.0 Parameter: " + parameterName, errorUri));
    }
}
