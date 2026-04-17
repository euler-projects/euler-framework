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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import org.eulerframework.security.authentication.apple.AppAttestRegistration;
import org.eulerframework.security.authentication.apple.AppAttestRegistrationService;
import org.eulerframework.security.authentication.apple.AppleAppAttestValidationService;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerOAuth2ErrorCodes;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;

/**
 * An {@link AuthenticationProvider} that verifies Client Attestation data for
 * {@code attest_jwt_client_auth} clients, as defined in
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-08.html">
 * draft-ietf-oauth-attestation-based-client-auth-08</a>.
 * <p>
 * This provider is registered with {@code OAuth2ClientAuthenticationFilter} and handles
 * the {@code attest_jwt_client_auth} authentication method (Section 6.3 / 13.4 of the draft).
 * It works in tandem with {@link org.eulerframework.security.oauth2.server.authorization.web.authentication.ClientAttestationAuthenticationConverter
 * ClientAttestationAuthenticationConverter}, which collects raw attestation data from
 * the request without any parsing or verification.
 * <p>
 * The provider performs all verification and resolution:
 * <ol>
 *   <li>Dispatches PoP verification by {@code popType}:
 *       <ul>
 *         <li>{@code jwt}: delegates to {@link ClientAttestationVerifier} which handles
 *             kid extraction, key lookup, and PoP JWT verification.</li>
 *         <li>{@code App-Attest}: looks up the registration by {@code key_id} and
 *             validates the assertion via {@link AppleAppAttestValidationService}.</li>
 *       </ul>
 *   </li>
 *   <li>Resolves the {@code client_id} from the verification result.</li>
 *   <li>Looks up the {@link RegisteredClient} and verifies it supports
 *       {@code attest_jwt_client_auth}.</li>
 *   <li>Validates RFC 6749 {@code client_id} consistency if the request carried one.</li>
 * </ol>
 * <p>
 * After successful authentication, the verified {@code key_id} is preserved as the
 * authenticated token's credentials for downstream components.
 *
 * @see org.eulerframework.security.oauth2.server.authorization.web.authentication.ClientAttestationAuthenticationConverter
 * @see ClientAttestationVerifier
 * @see EulerClientAuthenticationMethod#ATTEST_JWT_CLIENT_AUTH
 */
public final class ClientAttestationAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(ClientAttestationAuthenticationProvider.class);

    private final RegisteredClientRepository registeredClientRepository;
    private final AppAttestRegistrationService appAttestRegistrationService;
    private final AppleAppAttestValidationService appleAppAttestValidationService;

    @Nullable
    private ClientAttestationVerifier clientAttestationVerifier;

    public ClientAttestationAuthenticationProvider(
            RegisteredClientRepository registeredClientRepository,
            AppAttestRegistrationService appAttestRegistrationService,
            AppleAppAttestValidationService appleAppAttestValidationService) {
        Assert.notNull(registeredClientRepository, "registeredClientRepository must not be null");
        Assert.notNull(appAttestRegistrationService, "appAttestRegistrationService must not be null");
        Assert.notNull(appleAppAttestValidationService, "appleAppAttestValidationService must not be null");
        this.registeredClientRepository = registeredClientRepository;
        this.appAttestRegistrationService = appAttestRegistrationService;
        this.appleAppAttestValidationService = appleAppAttestValidationService;
    }

    public void setClientAttestationVerifier(@Nullable ClientAttestationVerifier clientAttestationVerifier) {
        this.clientAttestationVerifier = clientAttestationVerifier;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2ClientAuthenticationToken clientAuth = (OAuth2ClientAuthenticationToken) authentication;

        if (!EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH
                .equals(clientAuth.getClientAuthenticationMethod())) {
            return null;
        }

        Map<String, Object> additionalParams = clientAuth.getAdditionalParameters();
        String popType = (String) additionalParams.get(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP_TYPE);
        String popData = (String) additionalParams.get(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP);
        String attestationJwt = (String) additionalParams.get(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION);

        String resolvedKeyId = null;
        String resolvedClientId = null;

        // === 1. PoP verification (dispatched by popType) ===
        switch (popType) {

            case EulerOAuth2ParameterNames.POP_TYPE_JWT -> {
                // kid extraction & clientId resolution fully delegated to ClientAttestationVerifier
                if (popData == null) {
                    throw attestationError("PoP-Type=jwt but missing "
                            + EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP + " header");
                }
                if (this.clientAttestationVerifier == null) {
                    throw attestationError("ClientAttestationVerifier is not configured");
                }

                ClientAttestationVerifier.PopVerificationResult result;
                if (attestationJwt != null) {
                    result = this.clientAttestationVerifier.verify(attestationJwt, popData);
                } else {
                    result = this.clientAttestationVerifier.verify(popData);
                }
                resolvedKeyId = result.keyId();
                resolvedClientId = result.clientId();
            }

            case EulerOAuth2ParameterNames.POP_TYPE_APP_ATTEST -> {
                String keyId = (String) additionalParams.get(EulerOAuth2ParameterNames.KEY_ID);
                String assertion = (String) additionalParams.get(EulerOAuth2ParameterNames.ASSERTION);
                String challenge = (String) additionalParams.get(EulerOAuth2ParameterNames.CHALLENGE);

                if (!StringUtils.hasText(keyId) || !StringUtils.hasText(assertion)
                        || !StringUtils.hasText(challenge)) {
                    throw attestationError("App-Attest PoP requires "
                            + EulerOAuth2ParameterNames.KEY_ID + ", "
                            + EulerOAuth2ParameterNames.ASSERTION + ", and "
                            + EulerOAuth2ParameterNames.CHALLENGE + " parameters");
                }

                resolvedKeyId = keyId;
                AppAttestRegistration registration = this.appAttestRegistrationService.findByKeyId(keyId);
                if (registration == null) {
                    throw attestationError("Unknown key_id: " + keyId);
                }
                resolvedClientId = registration.getClientId();

                this.appleAppAttestValidationService.validateAssertion(keyId, assertion, challenge);
            }

            default -> throw attestationError("Unsupported PoP-Type: " + popType);
        }

        // === 2. Resolve RegisteredClient from the verified clientId ===
        if (resolvedClientId == null) {
            throw attestationError("Unable to resolve client_id from attestation data");
        }

        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(resolvedClientId);
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        if (!registeredClient.getClientAuthenticationMethods()
                .contains(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_CLIENT,
                    "Client does not support 'attest_jwt_client_auth' authentication method", null));
        }

        // === 3. RFC 6749 client_id consistency check ===
        String requestClientId = (String) additionalParams.get(OAuth2ParameterNames.CLIENT_ID);
        if (requestClientId != null && !requestClientId.equals(resolvedClientId)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_CLIENT, "client_id mismatch", null));
        }

        // Return authenticated token with keyId as credentials for downstream extraction
        return new OAuth2ClientAuthenticationToken(registeredClient,
                EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH, resolvedKeyId);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private static OAuth2AuthenticationException attestationError(String description) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION, description, null));
    }
}
