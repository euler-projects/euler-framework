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

import org.eulerframework.common.util.StringUtils;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.AppAttestUser;
import org.eulerframework.security.authentication.otp.OtpTicketService;
import org.eulerframework.security.authentication.otp.OtpVerification;
import org.eulerframework.security.core.EulerUser;
import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.core.identity.UserIdentity;
import org.eulerframework.security.core.identity.UserIdentityService;
import org.eulerframework.security.core.userdetails.EulerDeviceUserDetailsService;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.core.userdetails.RandomUsernameGenerator;
import org.eulerframework.security.core.userdetails.UserDetailsNotFoundException;
import org.eulerframework.security.oauth2.core.EulerAuthorizationGrantType;
import org.eulerframework.security.oauth2.server.authorization.web.EulerOAuth2AttestationBasedClientAuthenticationFilter;
import org.eulerframework.security.util.UserDetailsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import org.springframework.util.CollectionUtils;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@link AuthenticationProvider} for {@code grant_type=otp}.
 *
 * <p>Token issuance proceeds as follows:
 * <ol>
 *   <li>Resolve and verify the authenticated client (standard OAuth2
 *       client authentication step).</li>
 *   <li>Validate that requested scopes are a subset of those registered
 *       on the client.</li>
 *   <li>Atomically consume the {@code otp_ticket} via
 *       {@link OtpTicketService#consume(String, String, String, String)},
 *       performing OTP value match and PKCE {@code code_verifier} S256
 *       match. A {@code null} return surfaces as {@code invalid_grant}.</li>
 *   <li>Map {@link OtpVerification#channel()} to the target
 *       {@code identity_type} (sms &rarr; phone, email &rarr; email) and
 *       reverse-resolve the binding via
 *       {@link UserIdentityService#findUserIdentityByRawSubject(String, String)}.
 *       The grant does not assume any particular transform from the raw
 *       subject to the persisted {@code subject} &mdash; per-type
 *       backends decide whether to hash, normalise, or pass through.
 *       When the recipient is unknown, auto-provision a fresh user
 *       (username from {@link RandomUsernameGenerator#generate()},
 *       {@code {noop}}-prefixed random password, {@code "user"}
 *       authority) and bind the identity through the pre-verified
 *       prototype entry
 *       {@link UserIdentityService#createUserIdentity(String, UserIdentity)}.</li>
 *   <li>Load the resolved user via
 *       {@link EulerUserService#loadUserById(String)} and convert it
 *       with {@link UserDetailsUtils#toEulerUserDetails(EulerUser)}.</li>
 *   <li>When the request carries a verified App Attest device (set by
 *       {@link org.eulerframework.security.oauth2.server.authorization.web.EulerOAuth2AttestationBasedClientAuthenticationFilter}),
 *       enforce device-to-user consistency: a device already bound to a
 *       different user fails with {@code invalid_grant}
 *       ({@code "device mismatch"}); an unbound device is bound to the
 *       OTP-resolved user via
 *       {@link EulerDeviceUserDetailsService#bindToUser(AppAttestUser, String)}
 *       &mdash; distinct from
 *       {@link EulerDeviceUserDetailsService#createUser(AppAttestUser)},
 *       which provisions a brand-new anonymous user.</li>
 *   <li>Issue access, refresh and id tokens via the shared
 *       {@link OAuth2TokenGenerator}, mirroring the password grant.</li>
 * </ol>
 */
public class OAuth2OtpAuthenticationProvider implements AuthenticationProvider {

    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";
    private static final OAuth2TokenType ID_TOKEN_TOKEN_TYPE =
            new OAuth2TokenType(OidcParameterNames.ID_TOKEN);

    /**
     * Hard-coded {@code OTP channel -> identity_type} mapping. Values
     * use the public {@code identity_type} namespace surfaced on the
     * {@code /user/identities} REST surface; the identity SPI keys on
     * the same namespace.
     */
    private static final Map<String, String> CHANNEL_TO_IDENTITY_TYPE = Map.of(
            "sms", "phone",
            "email", "email"
    );

    private static final Map<String, String> CHANNEL_TO_RAW_SUB_PARAM_NAME = Map.of(
            "sms", "phone",
            "email", "email"
    );

    private final Logger logger = LoggerFactory.getLogger(OAuth2OtpAuthenticationProvider.class);

    private final OtpTicketService otpTicketService;
    /**
     * Identity SPI used to reverse-resolve the OTP recipient back to a
     * user and to auto-provision a binding when the recipient is
     * unknown.
     */
    private final UserIdentityService userIdentityService;
    private final EulerUserService eulerUserService;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    /**
     * Optional. When set, the provider enforces device-to-user
     * consistency for OTP requests carrying a verified App Attest
     * device; when {@code null}, attestation attached to OTP requests
     * is silently ignored.
     */
    private EulerDeviceUserDetailsService deviceUserDetailsService;

    public OAuth2OtpAuthenticationProvider(OtpTicketService otpTicketService,
                                           UserIdentityService userIdentityService,
                                           EulerUserService eulerUserService,
                                           OAuth2AuthorizationService authorizationService,
                                           OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
        Assert.notNull(otpTicketService, "otpTicketService must not be null");
        Assert.notNull(userIdentityService, "userIdentityService must not be null");
        Assert.notNull(eulerUserService, "eulerUserService must not be null");
        Assert.notNull(authorizationService, "authorizationService must not be null");
        Assert.notNull(tokenGenerator, "tokenGenerator must not be null");
        this.otpTicketService = otpTicketService;
        this.userIdentityService = userIdentityService;
        this.eulerUserService = eulerUserService;
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
        OAuth2OtpAuthenticationToken otpAuthenticationToken = (OAuth2OtpAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal =
                OAuth2AuthenticationProviderUtilsAccessor.getAuthenticatedClientElseThrowInvalidClient(otpAuthenticationToken);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        validateScope(otpAuthenticationToken, registeredClient);
        Set<String> authorizedScopes = Collections.unmodifiableSet(otpAuthenticationToken.getScopes());

        // 1. Atomically consume the OTP ticket. consume() performs OTP
        //    value match plus PKCE S256 (code_verifier vs stored
        //    code_challenge).
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

        // 2. Reverse-resolve (identity_type, recipient) -> userId via
        //    the identity SPI. The grant does not know how a per-type
        //    backend derives its persisted `subject` field (phone hash /
        //    email normalize+hash / wechat openid pass-through / ...);
        //    it merely picks the identity_type and asks who owns the
        //    raw value.
        String identityType = resolveIdentityType(verification.channel());
        String rawSubjectParamName = resolveRawSubjectParamName(verification.channel());
        String rawSubject = verification.recipient();
        UserIdentity identity = this.userIdentityService
                .findUserIdentityByRawSubject(identityType, rawSubject)
                .orElseGet(() -> autoProvisionUser(identityType, rawSubjectParamName, rawSubject));

        // 3. If the request carries a verified App Attest device (set by
        //    EulerOAuth2AttestationBasedClientAuthenticationFilter), enforce
        //    device-to-user consistency before token issuance.
        AppAttestAttestationRegistration verifiedAppRegistration =
                (AppAttestAttestationRegistration) otpAuthenticationToken.getAdditionalParameters()
                        .get(EulerOAuth2AttestationBasedClientAuthenticationFilter.VERIFIED_CLIENT_ATTESTATION_PARAMETER);
        enforceDeviceConsistency(verifiedAppRegistration, identity.getUserId());

        EulerUser eulerUser = this.eulerUserService.loadUserById(identity.getUserId());
        EulerUserDetails userDetails = UserDetailsUtils.toEulerUserDetails(eulerUser);
        if (userDetails == null || CollectionUtils.isEmpty(userDetails.getAuthorities())) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT,
                    "Failed to load user after auto-provision", ERROR_URI));
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

    private static String resolveIdentityType(String channel) {
        String identityType = CHANNEL_TO_IDENTITY_TYPE.get(channel);
        if (identityType == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_GRANT,
                    "Unsupported OTP channel: " + channel,
                    ERROR_URI));
        }
        return identityType;
    }

    private static String resolveRawSubjectParamName(String channel) {
        String attributeName = CHANNEL_TO_RAW_SUB_PARAM_NAME.get(channel);
        if (attributeName == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_GRANT,
                    "Unsupported OTP channel: " + channel,
                    ERROR_URI));
        }
        return attributeName;
    }

    /**
     * Auto-provision a fresh user and bind {@code (identityType, rawSubject)}
     * to it via the pre-verified prototype entry
     * {@link UserIdentityService#createUserIdentity(String, UserIdentity)}.
     *
     * <p>An OTP-grant request whose recipient is unknown is treated as
     * an implicit signup. The username is generated through
     * {@link RandomUsernameGenerator#generate()} (form
     * {@code user_<base64url12>}) so the recipient never leaks into
     * the local username; the password is a {@code {noop}}-prefixed
     * random string (OTP-only login, no password authentication path);
     * authorities default to {@code "user"}.
     *
     * <p>This grant handles {@code identity_type ∈ {phone, email}}.
     * For both, the raw subject is attached to the prototype as an
     * extension attribute whose key equals the {@code identity_type}
     * string itself (e.g. {@code "phone"} for the phone backend); the
     * backend reads it back under the same key.
     */
    private UserIdentity autoProvisionUser(String identityType, String rawSubjectParamName, String rawSubject) {
        EulerUserDetails newUser = EulerUserDetails.builder()
                .username(RandomUsernameGenerator.generate())
                .password("{noop}" + StringUtils.randomString(32))
                .authorities("user")
                .build();
        EulerUser createdUser = this.eulerUserService.createUser(newUser);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Auto-provisioned user '{}' for OTP identity_type='{}'",
                    createdUser.getUserId(), identityType);
        }
        UserIdentity prototype = UserIdentity.builder()
                .identityType(identityType)
                .property(rawSubjectParamName, rawSubject)
                .build();
        return this.userIdentityService.createUserIdentity(createdUser.getUserId(), prototype);
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
            // AppAttest registration provider's auto-create flow, which
            // would provision a brand-new anonymous user instead.
            this.deviceUserDetailsService.bindToUser(attestUser, otpUserId);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Bound App Attest device keyId='{}' to OTP-resolved user '{}'",
                        verifiedAppRegistration.getKeyId(), otpUserId);
            }
        }
    }
}
