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

import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.Map;

/**
 * Marks a client authentication in which the client already authenticated through a traditional
 * method and the client attestation was verified as an <i>additional security signal</i> on top of
 * it, per
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-08.html">
 * draft-ietf-oauth-attestation-based-client-auth-08</a> Section 6.4, rather than the attestation
 * being the client authentication method itself.
 * <p>
 * The type itself is the marker; it adds no field. Consequently
 * {@link #getClientAuthenticationMethod()} reports the traditional method the client actually
 * authenticated with, such as {@code client_secret_basic}, while {@link #getCredentials()} carries
 * the verified {@code AppAttestAttestationRegistration}. On a plain
 * {@link OAuth2ClientAuthenticationToken} that combination would be contradictory, which is what
 * this subtype exists to explain.
 * <p>
 * <b>Must never reach the shared client authentication {@code ProviderManager}.</b> Spring's client
 * authentication providers declare {@code supports()} over
 * {@link OAuth2ClientAuthenticationToken}, so they would claim this instance and, reading the
 * traditional method it now carries, treat it as an ordinary credential-based request. It is
 * therefore constructed only by
 * {@link org.eulerframework.security.oauth2.server.authorization.web.EulerOAuth2AttestationBasedClientAuthenticationFilter},
 * which calls {@link EulerOAuth2ClientAttestationAuthenticationProvider#authenticate} directly.
 *
 * @see EulerOAuth2ClientAttestationAuthenticationProvider
 */
public class EulerOAuth2ClientAttestationAdditionalSignalAuthenticationToken
        extends OAuth2ClientAuthenticationToken {

    /**
     * Constructs an unauthenticated request token, mirroring the base type's request form.
     * @param clientId the client identifier, or the converter's attestation placeholder
     * @param clientAuthenticationMethod the traditional method the client authenticated with
     * @param credentials the client credentials
     * @param additionalParameters the attestation data collected by the converter
     */
    public EulerOAuth2ClientAttestationAdditionalSignalAuthenticationToken(
            String clientId, ClientAuthenticationMethod clientAuthenticationMethod,
            Object credentials, Map<String, Object> additionalParameters) {
        super(clientId, clientAuthenticationMethod, credentials, additionalParameters);
    }

    /**
     * Constructs an authenticated result token, mirroring the base type's result form.
     * @param registeredClient the authenticated registered client
     * @param clientAuthenticationMethod the traditional method the client authenticated with
     * @param credentials the verified attestation registration
     */
    public EulerOAuth2ClientAttestationAdditionalSignalAuthenticationToken(
            RegisteredClient registeredClient, ClientAuthenticationMethod clientAuthenticationMethod,
            Object credentials) {
        super(registeredClient, clientAuthenticationMethod, credentials);
    }

}
