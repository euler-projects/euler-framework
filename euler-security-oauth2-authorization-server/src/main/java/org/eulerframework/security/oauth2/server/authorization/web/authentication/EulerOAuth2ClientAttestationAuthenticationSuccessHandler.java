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
package org.eulerframework.security.oauth2.server.authorization.web.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationAuthenticationToken;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationVerifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link AuthenticationSuccessHandler} for {@code OAuth2ClientAuthenticationFilter} that applies
 * a client attestation as an <b>additional signal</b> on top of a traditional client
 * authentication, as defined in Section 6.4 of
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-08.html">
 * draft-ietf-oauth-attestation-based-client-auth-08</a>.
 * <p>
 * It replaces the filter's default handler and therefore ends by publishing the authenticated client
 * to the {@code SecurityContext} exactly as that handler does. Running after the traditional
 * credential has been accepted also means a bad credential never burns the one-time challenge.
 * <p>
 * A client that authenticated with {@code attest_jwt_client_auth} is published unchanged: there the
 * attestation was the credential and
 * {@link org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationAuthenticationProvider}
 * already verified it. For any other method, when the request carries an attestation, this handler
 * verifies it, enforces the Section 6.4 requirement that it resolve to the very client that
 * authenticated, and republishes that client as an
 * {@link org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationAuthenticationToken}.
 * A request with no attestation is published unchanged.
 *
 * @see EulerOAuth2ClientAttestationVerifier
 * @see EulerOAuth2ClientAttestationAuthenticationConverter#collectAttestationParams
 */
public final class EulerOAuth2ClientAttestationAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final EulerOAuth2ClientAttestationVerifier clientAttestationVerifier;

    public EulerOAuth2ClientAttestationAuthenticationSuccessHandler(
            EulerOAuth2ClientAttestationVerifier clientAttestationVerifier) {
        Assert.notNull(clientAttestationVerifier, "clientAttestationVerifier must not be null");
        this.clientAttestationVerifier = clientAttestationVerifier;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        Authentication result = authentication;

        if (authentication instanceof OAuth2ClientAuthenticationToken clientAuthentication
                && !EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH
                        .equals(clientAuthentication.getClientAuthenticationMethod())
                && EulerOAuth2ClientAttestationAuthenticationConverter.carriesAttestationSignal(request)) {

            RegisteredClient registeredClient = clientAuthentication.getRegisteredClient();
            if (registeredClient != null) {
                Map<String, Object> attestationParams = new HashMap<>();
                EulerOAuth2ClientAttestationAuthenticationConverter.collectAttestationParams(request, attestationParams);
                EulerOAuth2ClientAttestationVerifier.ClientAttestationVerification verified =
                        this.clientAttestationVerifier.verify(attestationParams);

                // Section 6.4: an attestation presented alongside a traditional credential must
                // resolve to the very client that authenticated; a mismatch is rejected, not ignored.
                if (!registeredClient.getClientId().equals(verified.clientId())) {
                    throw new OAuth2AuthenticationException(
                            new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT, "client_id mismatch", null));
                }

                // Republish the client with the verified registration in a dedicated slot, keeping
                // the credential the traditional authentication produced.
                result = new EulerOAuth2ClientAttestationAuthenticationToken(registeredClient,
                        clientAuthentication.getClientAuthenticationMethod(),
                        clientAuthentication.getCredentials(), verified.registration(),
                        EulerOAuth2ClientAttestationAuthenticationConverter.resolveProof(attestationParams));
            }
        }

        // Replicates OAuth2ClientAuthenticationFilter's default success handler, which this replaces.
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(result);
        SecurityContextHolder.setContext(securityContext);
    }
}
