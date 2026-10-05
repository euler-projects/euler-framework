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

/**
 * The {@link AuthenticationSuccessHandler} for {@code OAuth2ClientAuthenticationFilter} that applies
 * a client attestation as an <b>additional signal</b> on top of a traditional client
 * authentication, as defined in Section 7.6 of
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-11.html">
 * draft-ietf-oauth-attestation-based-client-auth-11</a>.
 * <p>
 * It replaces the filter's default handler and therefore ends by publishing the authenticated client
 * to the {@code SecurityContext} exactly as that handler does. Running after the traditional
 * credential has been accepted also means a bad credential never burns the one-time challenge.
 * <p>
 * An {@link EulerOAuth2ClientAttestationAuthenticationToken} is published unchanged: the
 * attestation provider already verified its signal, whether it was the client's authentication
 * method or an additional signal on top of {@code none} + PKCE. For any other successful method,
 * when the request carries an attestation this handler verifies it, enforces the Section 7.6
 * requirement that it resolve to the very client that authenticated, and republishes that client as
 * an
 * {@link org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationAuthenticationToken}.
 * A request with no attestation is published unchanged.
 *
 * @see EulerOAuth2ClientAttestationVerifier
 * @see EulerOAuth2ClientAttestationAuthenticationConverter
 */
public final class EulerOAuth2ClientAttestationAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final EulerOAuth2ClientAttestationVerifier clientAttestationVerifier;
    private final EulerOAuth2ClientAttestationAuthenticationConverter clientAttestationConverter;

    public EulerOAuth2ClientAttestationAuthenticationSuccessHandler(
            EulerOAuth2ClientAttestationVerifier clientAttestationVerifier,
            EulerOAuth2ClientAttestationAuthenticationConverter clientAttestationConverter) {
        Assert.notNull(clientAttestationVerifier, "clientAttestationVerifier must not be null");
        Assert.notNull(clientAttestationConverter, "clientAttestationConverter must not be null");
        this.clientAttestationVerifier = clientAttestationVerifier;
        this.clientAttestationConverter = clientAttestationConverter;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        Authentication result = authentication;

        if (authentication instanceof OAuth2ClientAuthenticationToken clientAuthentication
                && !(authentication instanceof EulerOAuth2ClientAttestationAuthenticationToken)) {

            RegisteredClient registeredClient = clientAuthentication.getRegisteredClient();
            OAuth2ClientAuthenticationToken attestationAuthentication =
                    this.clientAttestationConverter.convert(request);
            if (registeredClient != null && attestationAuthentication != null) {
                EulerOAuth2ClientAttestationVerifier.ClientAttestationVerification verified =
                        this.clientAttestationVerifier.verify(attestationAuthentication);

                // Section 7.6: an attestation presented alongside a traditional credential must
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
                        OAuth2ClientAttestationUtils.resolveProof(
                                attestationAuthentication.getAdditionalParameters()));
            }
        }

        // Replicates OAuth2ClientAuthenticationFilter's default success handler, which this replaces.
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(result);
        SecurityContextHolder.setContext(securityContext);
    }
}
