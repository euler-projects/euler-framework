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

import org.eulerframework.security.authentication.otp.OtpTicketService;
import org.eulerframework.security.authentication.otp.OtpVerification;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.core.userdetails.EulerUserDetailsService;
import org.eulerframework.security.oauth2.core.EulerAuthorizationGrantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthenticationProviderUtilsAccessor;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@link AuthenticationProvider} for {@code grant_type=otp}.
 * <p>
 * Flow:
 * <ol>
 *     <li>Resolve and verify the authenticated client (standard OAuth2 client
 *         authentication step).</li>
 *     <li>Validate the requested scopes are a subset of those registered on
 *         the client.</li>
 *     <li>Atomically consume the {@code otp_ticket} via
 *         {@link OtpTicketService#consume(String, String, String, String)} -
 *         this performs OTP value match + PKCE {@code code_verifier} S256
 *         match. A {@code null} return is treated as a verification failure
 *         and surfaces as {@code invalid_grant}.</li>
 *     <li>Take {@link OtpVerification#recipient()} (a phone number under the
 *         current single-channel assumption) and load the user via
 *         {@link EulerUserDetailsService#loadUserByPrincipal(String)}.</li>
 *     <li>Issue access / refresh / id tokens using the shared
 *         {@link OAuth2TokenGenerator}, mirroring the password grant flow.</li>
 * </ol>
 */
public class OAuth2OtpAuthenticationProvider implements AuthenticationProvider {

    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";
    private static final OAuth2TokenType ID_TOKEN_TOKEN_TYPE =
            new OAuth2TokenType(OidcParameterNames.ID_TOKEN);

    private final Logger logger = LoggerFactory.getLogger(OAuth2OtpAuthenticationProvider.class);

    private final OtpTicketService otpTicketService;
    private final EulerUserDetailsService userDetailsService;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    public OAuth2OtpAuthenticationProvider(OtpTicketService otpTicketService,
                                           EulerUserDetailsService userDetailsService,
                                           OAuth2AuthorizationService authorizationService,
                                           OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
        Assert.notNull(otpTicketService, "otpTicketService must not be null");
        Assert.notNull(userDetailsService, "userDetailsService must not be null");
        Assert.notNull(authorizationService, "authorizationService must not be null");
        Assert.notNull(tokenGenerator, "tokenGenerator must not be null");
        this.otpTicketService = otpTicketService;
        this.userDetailsService = userDetailsService;
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2OtpAuthenticationToken otpAuthenticationToken = (OAuth2OtpAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal =
                OAuth2AuthenticationProviderUtilsAccessor.getAuthenticatedClientElseThrowInvalidClient(otpAuthenticationToken);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        validateScope(otpAuthenticationToken, registeredClient);
        Set<String> authorizedScopes = Collections.unmodifiableSet(otpAuthenticationToken.getScopes());

        // 1. Atomically consume the OTP ticket. consume() performs OTP value
        //    match + PKCE S256 (code_verifier vs stored code_challenge).
        OtpVerification verification;
        try {
            verification = this.otpTicketService.consume(
                    otpAuthenticationToken.getOtpTicket(),
                    otpAuthenticationToken.getCodeVerifier(),
                    otpAuthenticationToken.getOtp(),
                    null);
        } catch (RuntimeException e) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT,
                    "OTP verification failed", ERROR_URI), e);
        }
        if (verification == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT,
                    "OTP verification failed", ERROR_URI));
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Consumed OTP ticket id='{}' channel='{}'",
                    verification.ticketId(), verification.channel());
        }

        // 2. recipient (assumed phone number under the current single-channel
        //    contract) -> EulerUserDetails. We look up directly via the
        //    UserDetailsService rather than going through the password
        //    AuthenticationManager, because OTP grant has no password to
        //    verify - the OTP+PKCE step above is the authenticator.
        String recipient = verification.recipient();
        EulerUserDetails userDetails = this.userDetailsService.loadUserByPrincipal(recipient);
        if (userDetails == null || CollectionUtils.isEmpty(userDetails.getAuthorities())) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT,
                    "No user is bound to the OTP recipient", ERROR_URI));
        }

        UsernamePasswordAuthenticationToken userPrincipal = UsernamePasswordAuthenticationToken.authenticated(
                userDetails, null, userDetails.getAuthorities());

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(userPrincipal.getName())
                .authorizationGrantType(EulerAuthorizationGrantType.OTP)
                .authorizedScopes(authorizedScopes)
                .attribute(Principal.class.getName(), userPrincipal);

        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(userPrincipal)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorizationGrantType(EulerAuthorizationGrantType.OTP)
                .authorizedScopes(authorizedScopes)
                .authorizationGrant(otpAuthenticationToken);

        // ----- Access token -----
        OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
        if (generatedAccessToken == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                    "The token generator failed to generate the access token.", ERROR_URI));
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Generated access token");
        }

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());
        if (generatedAccessToken instanceof ClaimAccessor) {
            authorizationBuilder.token(accessToken, (metadata) ->
                    metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, ((ClaimAccessor) generatedAccessToken).getClaims()));
        } else {
            authorizationBuilder.accessToken(accessToken);
        }

        // ----- Refresh token -----
        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
            OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
            if (generatedRefreshToken != null) {
                if (!(generatedRefreshToken instanceof OAuth2RefreshToken)) {
                    throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                            "The token generator failed to generate a valid refresh token.", ERROR_URI));
                }

                if (this.logger.isTraceEnabled()) {
                    this.logger.trace("Generated refresh token");
                }

                refreshToken = (OAuth2RefreshToken) generatedRefreshToken;
                authorizationBuilder.refreshToken(refreshToken);
            }
        }

        // ----- ID token -----
        OidcIdToken idToken;
        if (tokenContext.getAuthorizedScopes().contains(OidcScopes.OPENID)) {
            tokenContext = tokenContextBuilder
                    .tokenType(ID_TOKEN_TOKEN_TYPE)
                    .authorization(authorizationBuilder.build())   // ID token customizer may need access to access/refresh token
                    .build();
            OAuth2Token generatedIdToken = this.tokenGenerator.generate(tokenContext);
            if (!(generatedIdToken instanceof Jwt)) {
                throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                        "The token generator failed to generate the ID token.", ERROR_URI));
            }

            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Generated id token");
            }

            idToken = new OidcIdToken(generatedIdToken.getTokenValue(), generatedIdToken.getIssuedAt(),
                    generatedIdToken.getExpiresAt(), ((Jwt) generatedIdToken).getClaims());
            authorizationBuilder.token(idToken, (metadata) ->
                    metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, idToken.getClaims()));
        } else {
            idToken = null;
        }

        OAuth2Authorization authorization = authorizationBuilder.build();
        this.authorizationService.save(authorization);

        Map<String, Object> additionalParameters = Collections.emptyMap();
        if (idToken != null) {
            additionalParameters = new HashMap<>();
            additionalParameters.put(OidcParameterNames.ID_TOKEN, idToken.getTokenValue());
        }

        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2OtpAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private void validateScope(OAuth2OtpAuthenticationToken token, RegisteredClient registeredClient) {
        Set<String> requestedScopes = token.getScopes();
        Set<String> allowedScopes = registeredClient.getScopes();
        if (!requestedScopes.isEmpty() && !allowedScopes.containsAll(requestedScopes)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Invalid request: requested scope is not allowed for registered client '{}'", registeredClient.getId());
            }
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE));
        }
    }
}
