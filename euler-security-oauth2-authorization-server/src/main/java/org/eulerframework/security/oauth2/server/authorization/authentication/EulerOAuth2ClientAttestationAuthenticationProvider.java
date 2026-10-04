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
import org.eulerframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientAttestationUtils;
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
 * An {@link AuthenticationProvider} that authenticates a client by its client attestation, i.e. one
 * whose client authentication method is {@code attest_jwt_client_auth} (the draft's standard PoP
 * JWT) or {@code attest_appattest_client_auth} (an Apple App Attest assertion as the proof of
 * possession), per
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-11.html">
 * draft-ietf-oauth-attestation-based-client-auth-11</a>. This is the <b>basic</b> path, in which the
 * attestation is the client's credential.
 * <p>
 * It is registered at the <i>end</i> of the provider chain and admits two token shapes, so a
 * converter's output and a provider's consumption are not one-to-one here:
 * <ol>
 *   <li>either attestation-based method, for a request whose attestation is its only credential. The
 *       {@code client_id} is not a reliable form parameter, so verifying the attestation is what
 *       resolves it.</li>
 *   <li>{@code NONE} carrying a {@code code_verifier}, for an {@code authorization_code} + PKCE
 *       request from a client whose real method is attestation-based and which
 *       {@code PublicClientAuthenticationProvider} therefore declines. That token carries
 *       {@code NONE} instead of the real method, so the client is what identifies the mechanism
 *       here.</li>
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

        final String clientId;
        final AppAttestAttestationRegistration registration;
        // The attestation-based method this request authenticates with: it decides how the
        // attestation is verified, and the client must actually declare it.
        final ClientAuthenticationMethod attestMethod;

        if (EulerClientAuthenticationMethod.isAttestationBased(method)) {
            // Basic path: the attestation is the credential and identifies the client, so verifying
            // it is what resolves the client_id (consumes the one-time challenge exactly once).
            attestMethod = method;
            EulerOAuth2ClientAttestationVerifier.ClientAttestationVerification verified =
                    this.clientAttestationVerifier.verify(params, attestMethod);
            clientId = verified.clientId();
            registration = verified.registration();
        } else if (ClientAuthenticationMethod.NONE.equals(method)
                && params.containsKey(PkceParameterNames.CODE_VERIFIER)) {
            // authorization_code + PKCE from a client whose real method is attestation-based,
            // declined by PublicClientAuthenticationProvider and routed here. The token therefore
            // carries NONE rather than that method, so the client itself is what says which
            // mechanism this request must be verified with.
            clientId = (String) clientAuthentication.getPrincipal();
            RegisteredClient candidate = this.registeredClientRepository.findByClientId(clientId);
            if (candidate == null
                    || candidate.getClientAuthenticationMethods().contains(ClientAuthenticationMethod.NONE)) {
                return null;
            }
            // RFC 7591 admits exactly one token_endpoint_auth_method per client, so an
            // attestation-based client has exactly one.
            attestMethod = candidate.getClientAuthenticationMethods().stream()
                    .filter(EulerClientAuthenticationMethod::isAttestationBased)
                    .findFirst()
                    .orElse(null);
            if (attestMethod == null) {
                return null;
            }
            registration = this.clientAttestationVerifier.verify(params, attestMethod).registration();
        } else {
            return null;
        }

        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }
        if (!registeredClient.getClientAuthenticationMethods().contains(attestMethod)) {
            throw invalidClient("authentication_method");
        }

        // Enforce PKCE for an authorization_code grant; authenticateIfAvailable self-gates to a
        // no-op for any other grant, so it is safe to call unconditionally.
        this.codeVerifierAuthenticator.authenticateIfAvailable(clientAuthentication, registeredClient);

        // The attestation is the credential here, so there is none to carry alongside it.
        return new EulerOAuth2ClientAttestationAuthenticationToken(registeredClient,
                attestMethod, null, registration,
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
