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

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.AppAttestUser;
import org.eulerframework.security.authentication.appattest.RegisteredApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
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

import org.eulerframework.security.core.userdetails.EulerDeviceUserDetailsService;
import org.eulerframework.security.core.userdetails.UserDetailsNotFoundException;
import org.eulerframework.security.oauth2.core.EulerAuthorizationGrantType;
import org.eulerframework.security.oauth2.core.EulerClientAttestationProof;
import org.eulerframework.security.oauth2.server.authorization.settings.EulerConfigurationSettingNames;
import org.eulerframework.security.oauth2.server.authorization.web.EulerOAuth2AttestationBasedClientAuthenticationFilter;
import org.eulerframework.security.provisioning.jit.JitProvisioningPolicy;

/**
 * Authentication provider for the {@code urn:ietf:params:oauth:grant-type:app_assertion} grant type.
 * <p>
 * This is a <b>thin layer</b> responsible only for anonymous user resolution and
 * token issuance. Assertion/challenge cryptographic verification is performed
 * upstream by {@code ClientAttestationFilter}.
 * <p>
 * Applies to <b>STATIC</b> App Attest clients only; DYNAMIC per-key clients are decoupled
 * from users and are rejected here. Any login factor the request may also carry is
 * <b>ignored</b>: the user is resolved solely from the device-to-user association.
 * <p>
 * Flow:
 * <ol>
 *   <li>Retrieve the already-authenticated {@code RegisteredClient}.</li>
 *   <li>Validate the grant type and requested scopes.</li>
 *   <li>Load the user associated with the device, provisioning an anonymous one only for an
 *       attestation request.</li>
 *   <li>Generate Access Token and ID Token (if openid scope). No Refresh Token is issued
 *       because every token request already requires full device attestation, making
 *       refresh tokens redundant.</li>
 * </ol>
 *
 * @deprecated see {@link EulerAuthorizationGrantType#APP_ASSERTION}.
 */
@Deprecated
public class OAuth2AppAssertionAuthenticationProvider implements AuthenticationProvider {
    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

    private final Logger logger = LoggerFactory.getLogger(OAuth2AppAssertionAuthenticationProvider.class);
    private static final OAuth2TokenType ID_TOKEN_TOKEN_TYPE =
            new OAuth2TokenType(OidcParameterNames.ID_TOKEN);

    private final EulerDeviceUserDetailsService userDetailsService;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    private final JitProvisioningPolicy jitProvisioning;

    private UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

    public OAuth2AppAssertionAuthenticationProvider(
            EulerDeviceUserDetailsService userDetailsService,
            OAuth2AuthorizationService authorizationService,
            OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
            JitProvisioningPolicy jitProvisioning) {
        Assert.notNull(userDetailsService, "userDetailsService must not be null");
        Assert.notNull(authorizationService, "authorizationService must not be null");
        Assert.notNull(tokenGenerator, "tokenGenerator must not be null");
        Assert.notNull(jitProvisioning, "jitProvisioning must not be null");
        this.userDetailsService = userDetailsService;
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
        this.jitProvisioning = jitProvisioning;
    }

    public void setUserDetailsChecker(UserDetailsChecker userDetailsChecker) {
        Assert.notNull(userDetailsChecker, "userDetailsChecker must not be null");
        this.userDetailsChecker = userDetailsChecker;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2AppAssertionAuthenticationToken assertionAuthenticationToken =
                (OAuth2AppAssertionAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal =
                OAuth2AuthenticationProviderUtilsAccessor.getAuthenticatedClientElseThrowInvalidClient(assertionAuthenticationToken);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        if (!registeredClient.getAuthorizationGrantTypes().contains(EulerAuthorizationGrantType.APP_ASSERTION)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        // Assertion-only renewal is a STATIC-client compatibility path. DYNAMIC per-key
        // clients are decoupled from users and must not resolve a user from the assertion
        // alone (they renew via refresh_token). The grant-type check above already excludes
        // them; this marker check is defense in depth against a client misconfigured with
        // the app_assertion grant.
        Object clientTypeMarker = registeredClient.getClientSettings()
                .getSetting(EulerConfigurationSettingNames.Client.APP_ATTEST_CLIENT_TYPE);
        if (RegisteredApp.OAuth2ClientType.DYNAMIC.name().equals(clientTypeMarker)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        this.validateScope(assertionAuthenticationToken, registeredClient);
        Set<String> authorizedScopes = Collections.unmodifiableSet(assertionAuthenticationToken.getScopes());

        // Resolve anonymous user by verified attestation registration. The
        // verified registration is propagated by
        // OAuth2AppAssertionAuthenticationConverter through the parent
        // additionalParameters map under VERIFIED_CLIENT_ATTESTATION_PARAMETER.
        AppAttestAttestationRegistration verifiedAppRegistration =
                (AppAttestAttestationRegistration) assertionAuthenticationToken.getAdditionalParameters()
                        .get(EulerOAuth2AttestationBasedClientAuthenticationFilter.VERIFIED_CLIENT_ATTESTATION_PARAMETER);
        if (verifiedAppRegistration == null) {
            // Should not happen: the converter rejects requests that have not
            // been verified by the attestation filter. Defensive guard only.
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
        }
        AppAttestUser attestUser = new AppAttestUser(
                verifiedAppRegistration.getKeyId(), verifiedAppRegistration.getTeamId(), verifiedAppRegistration.getBundleId(), verifiedAppRegistration.getPublicKey());
        EulerClientAttestationProof proof = (EulerClientAttestationProof) assertionAuthenticationToken
                .getAdditionalParameters()
                .get(EulerOAuth2AttestationBasedClientAuthenticationFilter.CLIENT_ATTESTATION_PROOF_PARAMETER);
        UserDetails user;
        try {
            user = this.userDetailsService.loadUserByDeviceUser(attestUser);
        } catch (UserDetailsNotFoundException e) {
            // Only an attestation request may establish the device-to-user association; see
            // EulerAuthorizationGrantType#APP_ASSERTION. A missing proof is treated as
            // assertion-only (fail-safe: never write).
            if (proof != EulerClientAttestationProof.ATTESTATION) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("KeyId '{}' is not associated with a user and the request carried no "
                            + "attestation; refusing to provision one", verifiedAppRegistration.getKeyId());
                }
                throw new OAuth2AuthenticationException(new OAuth2Error(
                        OAuth2ErrorCodes.INVALID_GRANT,
                        "device is not associated with a user; an attestation request is required first",
                        ERROR_URI));
            }
            if (!this.jitProvisioning.isEnabled()) {
                throw new OAuth2AuthenticationException(new OAuth2Error(
                        OAuth2ErrorCodes.INVALID_GRANT,
                        "unknown device and JIT provisioning is disabled", ERROR_URI));
            }
            // First-time use via attestation: JIT-provision an anonymous user and associate it
            // with the key, so that later assertion-only renewals can resolve it.
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("User not found for keyId '{}', provisioning new anonymous user", verifiedAppRegistration.getKeyId());
            }
            user = this.userDetailsService.createUser(attestUser,
                    this.jitProvisioning.getDefaultAuthorities());
        }

        this.userDetailsChecker.check(user);

        Authentication userPrincipal = UsernamePasswordAuthenticationToken.authenticated(
                user, null, user.getAuthorities());

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(userPrincipal.getName())
                .authorizationGrantType(EulerAuthorizationGrantType.APP_ASSERTION)
                .authorizedScopes(authorizedScopes)
                .attribute(Principal.class.getName(), userPrincipal);

        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(userPrincipal)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorizationGrantType(EulerAuthorizationGrantType.APP_ASSERTION)
                .authorizedScopes(authorizedScopes)
                .authorizationGrant(assertionAuthenticationToken);

        // ----- Access token -----
        OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
        if (generatedAccessToken == null) {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                    "The token generator failed to generate the access token.", ERROR_URI);
            throw new OAuth2AuthenticationException(error);
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

        // ----- ID token -----
        // NOTE: No refresh token is generated for device-assertion grant.
        // Each token request requires full attestation verification (kid + challenge +
        // either attestation or assertion), so a refresh token provides no additional security benefit.
        OidcIdToken idToken;
        if (tokenContext.getAuthorizedScopes().contains(OidcScopes.OPENID)) {
            // @formatter:off
            tokenContext = tokenContextBuilder
                    .tokenType(ID_TOKEN_TOKEN_TYPE)
                    .authorization(authorizationBuilder.build())
                    .build();
            // @formatter:on
            OAuth2Token generatedIdToken = this.tokenGenerator.generate(tokenContext);
            if (!(generatedIdToken instanceof Jwt)) {
                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                        "The token generator failed to generate the ID token.", ERROR_URI);
                throw new OAuth2AuthenticationException(error);
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
                registeredClient, clientPrincipal, accessToken, null, additionalParameters);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2AppAssertionAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private void validateScope(OAuth2AppAssertionAuthenticationToken authenticationToken,
                               RegisteredClient registeredClient) {
        Set<String> requestedScopes = authenticationToken.getScopes();
        Set<String> allowedScopes = registeredClient.getScopes();
        if (!requestedScopes.isEmpty() && !allowedScopes.containsAll(requestedScopes)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Invalid request: requested scope is not allowed for registered client '{}'",
                        registeredClient.getId());
            }
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE));
        }
    }
}
