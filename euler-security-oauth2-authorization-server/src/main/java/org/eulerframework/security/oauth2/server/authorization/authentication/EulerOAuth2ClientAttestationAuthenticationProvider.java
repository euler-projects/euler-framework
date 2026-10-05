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

import jakarta.annotation.Nonnull;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientAttestationUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.CodeVerifierAuthenticatorAccessor;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * An {@link AuthenticationProvider} that validates the proof carried by an attestation-based request
 * token. The request method is {@code attest_jwt_client_auth} (the draft's standard PoP JWT) or
 * {@code attest_appattest_client_auth} (an Apple App Attest assertion as the proof of possession),
 * per
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-11.html">
 * draft-ietf-oauth-attestation-based-client-auth-11</a>. Once the proof resolves the client, that
 * client's registered method decides whether the proof was the OAuth client authentication itself
 * or an additional signal on top of {@code none} + PKCE.
 * <p>
 * It is registered at the <i>end</i> of the provider chain, but receives only an
 * attestation-based request token: its converter is ordered before Spring's public-client converter
 * and after every traditional credential converter. It first verifies the attestation and resolves
 * the authoritative {@code client_id}, then interprets the registered client's one authentication
 * method:
 * <ol>
 *   <li>the client declares the request's attestation-based method &mdash; the attestation is the
 *       OAuth client authentication; PKCE is enforced when this is an authorization-code request</li>
 *   <li>the client declares {@code none} &mdash; the attestation is an additional security signal on
 *       top of public-client authentication, so authorization-code PKCE is required</li>
 *   <li>anything else &mdash; the request is rejected as using an unauthorized authentication method</li>
 * </ol>
 * Both successful paths return {@link EulerOAuth2ClientAttestationAuthenticationToken}, preventing
 * the success handler from verifying and consuming the same attestation a second time.
 *
 * @see EulerOAuth2ClientAttestationVerifier
 * @see EulerClientAuthenticationMethod#ATTEST_JWT_CLIENT_AUTH
 * @see EulerClientAuthenticationMethod#ATTEST_APPATTEST_CLIENT_AUTH
 */
public final class EulerOAuth2ClientAttestationAuthenticationProvider implements AuthenticationProvider {

    private final RegisteredClientRepository registeredClientRepository;
    private final EulerOAuth2ClientAttestationVerifier clientAttestationVerifier;
    private final CodeVerifierAuthenticatorAccessor codeVerifierAuthenticator;

    public EulerOAuth2ClientAttestationAuthenticationProvider(RegisteredClientRepository registeredClientRepository,
                                                              EulerOAuth2ClientAttestationVerifier clientAttestationVerifier,
                                                              OAuth2AuthorizationService authorizationService) {
        Assert.notNull(registeredClientRepository, "registeredClientRepository must not be null");
        Assert.notNull(clientAttestationVerifier, "clientAttestationVerifier must not be null");
        Assert.notNull(authorizationService, "authorizationService must not be null");
        this.registeredClientRepository = registeredClientRepository;
        this.clientAttestationVerifier = clientAttestationVerifier;
        this.codeVerifierAuthenticator = new CodeVerifierAuthenticatorAccessor(authorizationService);
    }

    @Override
    public Authentication authenticate(@Nonnull Authentication authentication) throws AuthenticationException {
        OAuth2ClientAuthenticationToken clientAuthentication = (OAuth2ClientAuthenticationToken) authentication;
        ClientAuthenticationMethod method = clientAuthentication.getClientAuthenticationMethod();
        Map<String, Object> params = clientAuthentication.getAdditionalParameters();

        if (!EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH.equals(method)
                && !EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH.equals(method)) {
            return null;
        }

        // The request is ours: verify its attestation exactly once and let that proof resolve the
        // authoritative client_id before consulting client metadata.
        EulerOAuth2ClientAttestationVerifier.ClientAttestationVerification verified =
                this.clientAttestationVerifier.verify(clientAuthentication);
        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(verified.clientId());
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        final ClientAuthenticationMethod authenticatedMethod;
        if (registeredClient.getClientAuthenticationMethods().contains(method)) {
            // The attestation is the client's configured authentication method. PKCE applies only
            // when this is an authorization-code request and otherwise remains a no-op.
            this.codeVerifierAuthenticator.authenticateIfAvailable(clientAuthentication, registeredClient);
            authenticatedMethod = method;
        } else if (registeredClient.getClientAuthenticationMethods().contains(ClientAuthenticationMethod.NONE)) {
            // The client is public: its authentication remains NONE, with the already verified
            // attestation carried as an additional signal. This mode is authorization-code + PKCE
            // only, so unlike the attestation-authentication branch it is required, not optional.
            this.codeVerifierAuthenticator.authenticateRequired(clientAuthentication, registeredClient);
            authenticatedMethod = ClientAuthenticationMethod.NONE;
        } else {
            throw invalidClient("authentication_method");
        }

        return new EulerOAuth2ClientAttestationAuthenticationToken(registeredClient,
                authenticatedMethod, null, verified.registration(),
                OAuth2ClientAttestationUtils.resolveProof(params));
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
}
