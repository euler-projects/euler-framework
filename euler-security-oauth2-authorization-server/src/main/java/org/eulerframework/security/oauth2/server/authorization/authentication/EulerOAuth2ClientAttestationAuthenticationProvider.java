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
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2HeaderNames;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.EulerOAuth2ClientAttestationAuthenticationConverter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.CodeVerifierAuthenticatorAccessor;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * An {@link AuthenticationProvider} that authenticates a client whose client authentication method
 * is {@code attest_jwt_client_auth}, as defined in
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-08.html">
 * draft-ietf-oauth-attestation-based-client-auth-08</a>. This is the <b>basic</b> path, in which the
 * attestation is the client's credential.
 * <p>
 * It is registered at the <i>end</i> of the provider chain and admits two token shapes, so a
 * converter's output and a provider's consumption are not one-to-one here:
 * <ol>
 *   <li>{@code attest_jwt_client_auth}, for a request whose attestation is its only credential. The
 *       {@code client_id} is not a reliable form parameter, so verifying the attestation is what
 *       resolves it.</li>
 *   <li>{@code NONE} carrying an attestation and a {@code code_verifier}, for an
 *       {@code authorization_code} + PKCE request from a client whose real method is
 *       {@code attest_jwt_client_auth} and which {@code PublicClientAuthenticationProvider}
 *       therefore declines.</li>
 * </ol>
 * In both cases this provider verifies the attestation exactly once, resolves and validates the
 * client, and enforces PKCE via {@link CodeVerifierAuthenticatorAccessor} &mdash; these clients are
 * provisioned with {@code requireProofKey}.
 * <p>
 * The {@code NONE} branch looks the client up <i>before</i> verifying and declines when the client
 * really does declare {@code NONE}, so a genuine public client keeps its own outcome and the
 * one-time challenge is not burned on a request this provider does not own. Any other method returns
 * {@code null}.
 *
 * @see EulerOAuth2ClientAttestationVerifier
 * @see EulerClientAuthenticationMethod#ATTEST_JWT_CLIENT_AUTH
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

        final String clientId;
        final AppAttestAttestationRegistration registration;

        if (EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH.equals(method)) {
            // Basic path: the attestation is the credential and identifies the client, so verifying
            // it is what resolves the client_id (consumes the one-time challenge exactly once).
            EulerOAuth2ClientAttestationVerifier.ClientAttestationVerification verified =
                    this.clientAttestationVerifier.verify(params);
            clientId = verified.clientId();
            registration = verified.registration();
        } else if (ClientAuthenticationMethod.NONE.equals(method)
                && params.containsKey(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE)
                && params.containsKey(PkceParameterNames.CODE_VERIFIER)) {
            // authorization_code + PKCE from a client whose real method is attest_jwt_client_auth,
            // declined by PublicClientAuthenticationProvider and routed here. Look the client up
            // before verifying so a genuine public client is left to its own outcome.
            clientId = (String) clientAuthentication.getPrincipal();
            RegisteredClient candidate = this.registeredClientRepository.findByClientId(clientId);
            if (candidate == null
                    || candidate.getClientAuthenticationMethods().contains(ClientAuthenticationMethod.NONE)
                    || !candidate.getClientAuthenticationMethods()
                            .contains(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH)) {
                return null;
            }
            registration = this.clientAttestationVerifier.verify(params).registration();
        } else {
            return null;
        }

        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }
        if (!registeredClient.getClientAuthenticationMethods()
                .contains(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH)) {
            throw invalidClient("authentication_method");
        }

        // Enforce PKCE for an authorization_code grant; authenticateIfAvailable self-gates to a
        // no-op for any other grant, so it is safe to call unconditionally.
        this.codeVerifierAuthenticator.authenticateIfAvailable(clientAuthentication, registeredClient);

        // The attestation is the credential here, so there is none to carry alongside it.
        return new EulerOAuth2ClientAttestationAuthenticationToken(registeredClient,
                EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH, null, registration,
                EulerOAuth2ClientAttestationAuthenticationConverter.resolveProof(params));
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
