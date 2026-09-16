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
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistrationService;
import org.eulerframework.security.authentication.appattest.AppAttestUtils;
import org.eulerframework.security.authentication.appattest.RegisteredApp;
import org.eulerframework.security.authentication.appattest.RegisteredAppRepository;
import org.eulerframework.security.authentication.appattest.apple.AppleAppAttestValidationService;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerOAuth2ErrorCodes;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2HeaderNames;
import org.eulerframework.security.oauth2.server.authorization.converter.EulerOAuth2ClientRegistrationRegisteredClientConverter;
import org.eulerframework.security.oauth2.server.authorization.converter.EulerRegisteredClientOAuth2ClientRegistrationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.OAuth2ClientRegistration;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientRegistrationAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientRegistrationAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * An {@link AuthenticationProvider} that authenticates an RFC 7591 dynamic client registration
 * request by Apple App Attest device assertion, provisioning a per-KEY OAuth2 client for any
 * OAuth2-enabled app.
 * <p>
 * This is an alternative credential for the registration endpoint rather than a replacement of it:
 * it is registered alongside {@link OAuth2ClientRegistrationAuthenticationProvider}, which keeps
 * handling the RFC 7591 initial access token path, and the two are separated by
 * {@code supports()} since this provider only accepts
 * {@link EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken}. Both run inside
 * Spring's {@code OAuth2ClientRegistrationEndpointFilter}, so the endpoint keeps its standard
 * HTTP 201 success response and error format.
 * <p>
 * The assertion proves possession of a KEY already registered via {@code POST /app_attest/register};
 * the attestation itself is single-use and is not accepted here. This mirrors the OAuth 2.0
 * Attestation-Based Client Authentication message family used at the token endpoint, adapted to the
 * JSON registration body by carrying the proof in headers.
 * <p>
 * On success a per-KEY client is minted (random {@code client_id}, authentication method
 * {@code attest_jwt_client_auth}, {@code refresh_token} added) and its {@code client_id} is bound
 * back to the KEY registration. A KEY that is already bound returns its existing client, so
 * registration retries are idempotent.
 *
 * @see EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken
 */
public final class EulerOAuth2AttestationBasedClientRegistrationAuthenticationProvider
        implements AuthenticationProvider {

    private static final Duration DEFAULT_REFRESH_TOKEN_TTL = Duration.ofDays(30);

    private final Logger logger =
            LoggerFactory.getLogger(EulerOAuth2AttestationBasedClientRegistrationAuthenticationProvider.class);

    private final ChallengeService challengeService;
    private final AppleAppAttestValidationService validationService;
    private final RegisteredAppRepository registeredAppRepository;
    private final AppAttestAttestationRegistrationService registrationService;
    private final RegisteredClientRepository registeredClientRepository;

    private final Converter<OAuth2ClientRegistration, RegisteredClient> registeredClientConverter =
            new EulerOAuth2ClientRegistrationRegisteredClientConverter();
    private final Converter<RegisteredClient, OAuth2ClientRegistration> clientRegistrationResponseConverter =
            new EulerRegisteredClientOAuth2ClientRegistrationConverter();

    public EulerOAuth2AttestationBasedClientRegistrationAuthenticationProvider(
            ChallengeService challengeService,
            AppleAppAttestValidationService validationService,
            RegisteredAppRepository registeredAppRepository,
            AppAttestAttestationRegistrationService registrationService,
            RegisteredClientRepository registeredClientRepository) {
        Assert.notNull(challengeService, "challengeService must not be null");
        Assert.notNull(validationService, "validationService must not be null");
        Assert.notNull(registeredAppRepository, "registeredAppRepository must not be null");
        Assert.notNull(registrationService, "registrationService must not be null");
        Assert.notNull(registeredClientRepository, "registeredClientRepository must not be null");
        this.challengeService = challengeService;
        this.validationService = validationService;
        this.registeredAppRepository = registeredAppRepository;
        this.registrationService = registrationService;
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken clientRegistrationAuthentication =
                (EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken) authentication;

        String challenge = clientRegistrationAuthentication.getChallenge();
        if (!this.challengeService.consumeChallenge(challenge)) {
            throw invalidClientAttestation(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE);
        }

        AppAttestAttestationRegistration registration = this.validationService.validateAssertion(
                clientRegistrationAuthentication.getKeyId(),
                clientRegistrationAuthentication.getAssertion(),
                challenge);

        if (!isOAuth2EnabledApp(registration)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
                    "app is not enabled for OAuth2 client registration", null));
        }

        RegisteredClient registeredClient = resolveOrProvisionClient(
                clientRegistrationAuthentication.getClientRegistration(), registration);

        OAuth2ClientRegistration clientRegistrationResponse =
                this.clientRegistrationResponseConverter.convert(registeredClient);

        OAuth2ClientRegistrationAuthenticationToken clientRegistrationAuthenticationResult =
                new OAuth2ClientRegistrationAuthenticationToken(null, clientRegistrationResponse);
        clientRegistrationAuthenticationResult.setDetails(clientRegistrationAuthentication.getDetails());
        return clientRegistrationAuthenticationResult;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * Whether the {@link RegisteredApp} owning the KEY is OAuth2-enabled, and so is allowed to
     * mint a per-KEY client through this endpoint.
     */
    private boolean isOAuth2EnabledApp(AppAttestAttestationRegistration registration) {
        byte[] appIdHash = AppAttestUtils.appIdHash(registration.getTeamId() + "." + registration.getBundleId());
        RegisteredApp app = this.registeredAppRepository.findByAppIdHash(appIdHash);
        return app != null && app.isOauth2Enabled();
    }

    /**
     * Return the client already bound to the KEY (idempotent retry), or mint a new per-KEY client
     * from the parsed RFC 7591 request, save it and bind its {@code client_id} back to the KEY
     * registration.
     */
    private RegisteredClient resolveOrProvisionClient(OAuth2ClientRegistration clientRegistration,
                                                      AppAttestAttestationRegistration registration) {
        String boundClientId = registration.getClientId();
        if (StringUtils.hasText(boundClientId)) {
            RegisteredClient existing = this.registeredClientRepository.findByClientId(boundClientId);
            if (existing == null) {
                throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT,
                        "bound client_id no longer exists", null));
            }
            return existing;
        }

        RegisteredClient base = this.registeredClientConverter.convert(clientRegistration);

        RegisteredClient registeredClient = RegisteredClient.from(base)
                // Force attest-only client authentication: the base converter defaults to
                // client_secret_basic (with a generated secret) when the request omits
                // token_endpoint_auth_method, which does not apply to an App Attest client.
                .clientSecret(null)
                .clientAuthenticationMethods(methods -> {
                    methods.clear();
                    methods.add(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH);
                })
                .authorizationGrantTypes(grantTypes -> {
                    // Per-KEY clients renew via refresh_token + assertion. The deprecated
                    // app_assertion grant is deliberately not stripped here: it is rejected at the
                    // persistence layer (EulerOAuth2ClientService), so no entry point — this one
                    // included — can provision a client carrying it.
                    grantTypes.add(AuthorizationGrantType.REFRESH_TOKEN);
                })
                .tokenSettings(TokenSettings.builder()
                        .reuseRefreshTokens(false)
                        .refreshTokenTimeToLive(DEFAULT_REFRESH_TOKEN_TTL)
                        .build())
                .build();

        this.registeredClientRepository.save(registeredClient);
        this.registrationService.bindClientId(registration.getKeyId(), registeredClient.getClientId());
        this.logger.info("Provisioned DYNAMIC OAuth2 client '{}' for App Attest keyId '{}'",
                registeredClient.getClientId(), registration.getKeyId());
        return registeredClient;
    }

    private static OAuth2AuthenticationException invalidClientAttestation(String parameterName) {
        OAuth2Error error = new OAuth2Error(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION,
                "Client attestation failed: " + parameterName, null);
        return new OAuth2AuthenticationException(error);
    }

}
