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
import java.util.Optional;

import jakarta.annotation.Nonnull;
import org.eulerframework.security.oauth2.core.EulerOAuth2ClientAttestationType;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.EulerOAuth2ClientAttestationAuthenticationConverter;
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

import org.eulerframework.security.authentication.device.DeviceAttestRegistration;
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
 * It works in tandem with {@link EulerOAuth2ClientAttestationAuthenticationConverter},
 * which collects raw attestation data from the request without any parsing or verification.
 * <p>
 * The provider performs all verification and resolution:
 * <ol>
 *   <li>Dispatches PoP verification by {@code popType}:
 *       <ul>
 *         <li>{@code jwt}: delegates to {@link EulerOAuth2ClientAttestationVerifier} which handles
 *             kid extraction, key lookup, and PoP JWT verification.</li>
 *         <li>{@code app-attest}: looks up the registration by {@code kid} and
 *             validates the assertion via {@link AppleAppAttestValidationService}.
 *             Only available when App Attest is enabled (requires a non-null
 *             {@code AppleAppAttestValidationService}).</li>
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
 * @see EulerOAuth2ClientAttestationAuthenticationConverter
 * @see EulerOAuth2ClientAttestationVerifier
 * @see EulerClientAuthenticationMethod#ATTEST_JWT_CLIENT_AUTH
 */
public final class EulerOAuth2ClientAttestationAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(EulerOAuth2ClientAttestationAuthenticationProvider.class);

    private final RegisteredClientRepository registeredClientRepository;
    private final EulerOAuth2ClientAttestationVerifier oauth2ClientAttestationVerifier;
    private AppleAppAttestValidationService appleAppAttestValidationService;

    public EulerOAuth2ClientAttestationAuthenticationProvider(
            RegisteredClientRepository registeredClientRepository,
            EulerOAuth2ClientAttestationVerifier oauth2ClientAttestationVerifier) {
        Assert.notNull(registeredClientRepository, "registeredClientRepository must not be null");
        Assert.notNull(oauth2ClientAttestationVerifier, "clientAttestationVerifier must not be null");
        this.registeredClientRepository = registeredClientRepository;
        this.oauth2ClientAttestationVerifier = oauth2ClientAttestationVerifier;
    }

    public void setAppleAppAttestValidationService(AppleAppAttestValidationService appleAppAttestValidationService) {
        this.appleAppAttestValidationService = appleAppAttestValidationService;
    }

    public EulerOAuth2ClientAttestationVerifier getOauth2ClientAttestationVerifier() {
        return oauth2ClientAttestationVerifier;
    }

    @Override
    public Authentication authenticate(@Nonnull Authentication authentication) throws AuthenticationException {
        OAuth2ClientAuthenticationToken clientAuthentication = (OAuth2ClientAuthenticationToken) authentication;

        if (!EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH
                .equals(clientAuthentication.getClientAuthenticationMethod())) {
            return null;
        }

        Map<String, Object> additionalParams = clientAuthentication.getAdditionalParameters();
        EulerOAuth2ClientAttestationType auth2ClientAttestationType = (EulerOAuth2ClientAttestationType) additionalParams
                .get(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP_TYPE);

        final String resolvedKeyId;
        final String resolvedClientId;

        if (EulerOAuth2ClientAttestationType.JWT.equals(auth2ClientAttestationType)) {
            String attestationJwt = (String) additionalParams.get(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION);
            String attestationPopJwt = (String) additionalParams.get(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP);

            if (attestationPopJwt == null) {
                throw invalidClientAttestation(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP);
            }

            EulerOAuth2ClientAttestationVerifier.PopVerificationResult result = attestationJwt == null
                    ? this.oauth2ClientAttestationVerifier.verify(attestationPopJwt)
                    : this.oauth2ClientAttestationVerifier.verify(attestationJwt, attestationPopJwt);

            resolvedKeyId = result.keyId();
            resolvedClientId = result.clientId();
        } else if (EulerOAuth2ClientAttestationType.APP_ATTEST.equals(auth2ClientAttestationType)) {
            if (this.appleAppAttestValidationService == null) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION,
                                "APP_ATTEST attestation type is not supported; "
                                        + "enable euler.security.device-attest to use this attestation type", null));
            }

            String keyId = (String) additionalParams.get(EulerOAuth2ParameterNames.KEY_ID);
            String attestation = (String) additionalParams.get(EulerOAuth2ParameterNames.ATTESTATION);
            String assertion = (String) additionalParams.get(EulerOAuth2ParameterNames.ASSERTION);
            String challenge = (String) additionalParams.get(EulerOAuth2ParameterNames.CHALLENGE);

            // TODO: Apple App Attest Attestation during OAuth2 Client Attestation is not yet supported
            if (StringUtils.hasText(attestation)) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                                "Passing Apple App Attest Attestation during OAuth2 Client Attestation is not yet supported", null)
                );
            }

            if (!StringUtils.hasText(keyId)) {
                throw invalidClientAttestation(EulerOAuth2ParameterNames.KEY_ID);
            }

            if (!StringUtils.hasText(assertion)) {
                throw invalidClientAttestation(EulerOAuth2ParameterNames.ASSERTION);
            }

            if (!StringUtils.hasText(challenge)) {
                throw invalidClientAttestation(EulerOAuth2ParameterNames.CHALLENGE);
            }

            DeviceAttestRegistration registration = this.appleAppAttestValidationService.validateAssertion(keyId, assertion, challenge);

            resolvedKeyId = registration.getKeyId();
            resolvedClientId = registration.getClientId();
        } else {
            throw invalidClientAttestation(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP_TYPE);
        }

        if (resolvedClientId == null) {
            throw invalidClientAttestation(OAuth2ParameterNames.CLIENT_ID);
        }

        // Draft Section 6.3: If the token request contains a client_id parameter as per [RFC6749],
        // the Authorization Server MUST verify that the value of this parameter is the same as
        // the client_id value in the sub claim of the Client Attestation.
        String requestClientId = (String) additionalParams.get(OAuth2ParameterNames.CLIENT_ID);
        if (requestClientId != null && !requestClientId.equals(resolvedClientId)) {
            throw invalidClient("client_id mismatch");
        }

        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(resolvedClientId);
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        boolean additionalSecuritySignal = Optional.ofNullable(additionalParams.get(EulerOAuth2ParameterNames.ADDITIONAL_SECURITY_SIGNAL))
                .map(Boolean.class::cast)
                .orElse(false);

        if (!registeredClient.getClientAuthenticationMethods()
                .contains(clientAuthentication.getClientAuthenticationMethod())
                && !additionalSecuritySignal) {
            throw invalidClient("authentication_method");
        }

        // Return authenticated token with keyId as credentials for downstream extraction
        return new OAuth2ClientAuthenticationToken(registeredClient,
                EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH, resolvedKeyId);
    }

    @Override
    public boolean supports(@Nonnull Class<?> authentication) {
        return OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication);
    }


    private static OAuth2AuthenticationException invalidClient(String parameterName) {
        OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT,
                "Client authentication failed: " + parameterName, null);
        return new OAuth2AuthenticationException(error);
    }

    private static OAuth2AuthenticationException invalidClientAttestation(String parameterName) {
        OAuth2Error error = new OAuth2Error(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION,
                "Client attestation failed: " + parameterName, null);
        return new OAuth2AuthenticationException(error);
    }
}
