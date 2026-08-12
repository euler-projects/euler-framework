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
import org.eulerframework.security.authentication.otp.OneTimePasswordAuthenticationToken;
import org.eulerframework.security.core.userdetails.EulerDeviceUserDetailsService;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.core.userdetails.UserDetailsNotFoundException;
import org.eulerframework.security.oauth2.core.EulerAuthorizationGrantType;
import org.eulerframework.security.oauth2.server.authorization.web.EulerOAuth2AttestationBasedClientAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.*;
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

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@link AuthenticationProvider} for {@code grant_type=otp} on the token
 * endpoint. Issues access, refresh and id tokens once the submitted
 * one-time password is authenticated; a failed verification surfaces as
 * {@code invalid_grant}.
 * <p>
 * When the request carries a verified App Attest device, device-to-user
 * consistency is enforced: a device bound to a different user is rejected
 * with {@code invalid_grant}, and an unbound device is bound to the resolved
 * user on first use.
 */
public class OAuth2OneTimePasswordAuthenticationProvider implements AuthenticationProvider {

    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";
    private static final OAuth2TokenType ID_TOKEN_TOKEN_TYPE =
            new OAuth2TokenType(OidcParameterNames.ID_TOKEN);

    private final Logger logger = LoggerFactory.getLogger(OAuth2OneTimePasswordAuthenticationProvider.class);

    private final AuthenticationManager authenticationManager;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    /**
     * Optional. When set, the provider enforces device-to-user
     * consistency for OTP requests carrying a verified App Attest
     * device; when {@code null}, attestation attached to OTP requests
     * is silently ignored.
     */
    private EulerDeviceUserDetailsService deviceUserDetailsService;

    public OAuth2OneTimePasswordAuthenticationProvider(AuthenticationManager authenticationManager,
                                                       OAuth2AuthorizationService authorizationService,
                                                       OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
        Assert.notNull(authenticationManager, "authenticationManager must not be null");
        Assert.notNull(authorizationService, "authorizationService must not be null");
        Assert.notNull(tokenGenerator, "tokenGenerator must not be null");
        this.authenticationManager = authenticationManager;
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
    }

    /**
     * Configure the optional {@link EulerDeviceUserDetailsService} used
     * to enforce device-to-user consistency for OTP requests carrying a
     * verified App Attest device. When unset, attestation attached to
     * the request is ignored.
     */
    public void setDeviceUserDetailsService(EulerDeviceUserDetailsService deviceUserDetailsService) {
        this.deviceUserDetailsService = deviceUserDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2OneTimePasswordAuthenticationToken otpAuthenticationToken = (OAuth2OneTimePasswordAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal =
                OAuth2AuthenticationProviderUtilsAccessor.getAuthenticatedClientElseThrowInvalidClient(otpAuthenticationToken);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        validateScope(otpAuthenticationToken, registeredClient);
        Set<String> authorizedScopes = Collections.unmodifiableSet(otpAuthenticationToken.getScopes());

        // Complete the user-level one-time-password authentication through
        // the shared AuthenticationManager.
        Authentication userPrincipal;
        try {
            userPrincipal = this.authenticationManager.authenticate(otpAuthenticationToken.getUserPrincipal());
        } catch (AuthenticationException e) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT,
                    "OTP verification failed", ERROR_URI), e);
        }
        if (!userPrincipal.isAuthenticated()) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);
        }

        // If the request carries a verified App Attest device (set by
        // EulerOAuth2AttestationBasedClientAuthenticationFilter), enforce
        // device-to-user consistency before token issuance.
        AppAttestAttestationRegistration verifiedAppRegistration =
                (AppAttestAttestationRegistration) otpAuthenticationToken.getAdditionalParameters()
                        .get(EulerOAuth2AttestationBasedClientAuthenticationFilter.VERIFIED_CLIENT_ATTESTATION_PARAMETER);
        OneTimePasswordAuthenticationToken otpResult = (OneTimePasswordAuthenticationToken) userPrincipal;
        enforceDeviceConsistency(verifiedAppRegistration, otpResult.getUserIdentity().getUserId());

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
        return OAuth2OneTimePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private void validateScope(OAuth2OneTimePasswordAuthenticationToken token, RegisteredClient registeredClient) {
        Set<String> requestedScopes = token.getScopes();
        Set<String> allowedScopes = registeredClient.getScopes();
        if (!requestedScopes.isEmpty() && !allowedScopes.containsAll(requestedScopes)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Invalid request: requested scope is not allowed for registered client '{}'", registeredClient.getId());
            }
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE));
        }
    }

    /**
     * Enforce device-to-user consistency when an OTP request carries a
     * verified App Attest device. {@code verifiedAppRegistration} is
     * set by
     * {@link org.eulerframework.security.oauth2.server.authorization.web.EulerOAuth2AttestationBasedClientAuthenticationFilter}
     * and may be {@code null} for legacy clients.
     *
     * <p>Behaviour:
     * <ul>
     *   <li>{@code verifiedAppRegistration == null} &rarr; no-op.</li>
     *   <li>{@link #deviceUserDetailsService} not set &rarr; no-op
     *       (attestation is silently ignored).</li>
     *   <li>Device already bound to a user other than {@code otpUserId}
     *       &rarr; reject with {@code invalid_grant} /
     *       {@code description="device mismatch"}.</li>
     *   <li>Device not yet bound &rarr; bind it to {@code otpUserId} via
     *       {@link EulerDeviceUserDetailsService#bindToUser(AppAttestUser, String)},
     *       distinct from
     *       {@link EulerDeviceUserDetailsService#createUser(AppAttestUser)},
     *       which would provision a brand-new anonymous user.</li>
     * </ul>
     */
    private void enforceDeviceConsistency(AppAttestAttestationRegistration verifiedAppRegistration, String otpUserId) {
        if (verifiedAppRegistration == null || this.deviceUserDetailsService == null) {
            return;
        }

        AppAttestUser attestUser = new AppAttestUser(
                verifiedAppRegistration.getKeyId(),
                verifiedAppRegistration.getTeamId(),
                verifiedAppRegistration.getBundleId(),
                verifiedAppRegistration.getPublicKey());
        try {
            EulerUserDetails deviceBoundUser = this.deviceUserDetailsService.loadUserByDeviceUser(attestUser);
            if (!otpUserId.equals(deviceBoundUser.getUserId())) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Device mismatch: keyId='{}' is bound to user '{}', OTP resolved to user '{}'",
                            verifiedAppRegistration.getKeyId(), deviceBoundUser.getUserId(), otpUserId);
                }
                throw new OAuth2AuthenticationException(new OAuth2Error(
                        OAuth2ErrorCodes.INVALID_GRANT, "device mismatch", ERROR_URI));
            }
        } catch (UserDetailsNotFoundException ex) {
            // First sighting of this device with the OTP-resolved user:
            // bind the device to the existing user. Distinct from the
            // AppAttest registration provider's JIT provisioning flow,
            // which would provision a brand-new anonymous user instead.
            this.deviceUserDetailsService.bindToUser(attestUser, otpUserId);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Bound App Attest device keyId='{}' to OTP-resolved user '{}'",
                        verifiedAppRegistration.getKeyId(), otpUserId);
            }
        }
    }
}
