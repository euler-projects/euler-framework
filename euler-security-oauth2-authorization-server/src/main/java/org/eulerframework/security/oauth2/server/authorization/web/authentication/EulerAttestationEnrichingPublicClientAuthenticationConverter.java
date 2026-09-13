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
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Decorates Spring's {@code PublicClientAuthenticationConverter} to <i>enrich</i> the token it
 * produces with the request's client attestation data, without ever suppressing it.
 * <p>
 * {@code PublicClientAuthenticationConverter} claims on the <i>shape</i> of a request &mdash; an
 * {@code authorization_code} grant carrying a {@code code} and a {@code code_verifier} &mdash; rather
 * than on a presented credential. When such a request also carries an attestation, the attestation
 * has to travel <i>in the token</i>, because a provider has no access to the request; whether it ends
 * up as an additional signal or as the client's actual credential is decided downstream by
 * {@link EulerOAuth2ClientAttestationAuthenticationProvider}. The token's own parameters are
 * immutable, so it is rebuilt.
 * <p>
 * The grant parameters PKCE enforcement needs are already in the token, since the delegate copies
 * every form parameter.
 *
 * @see EulerOAuth2ClientAttestationAuthenticationConverter#collectAttestationParams
 */
public final class EulerAttestationEnrichingPublicClientAuthenticationConverter implements AuthenticationConverter {

    private final AuthenticationConverter delegate;

    public EulerAttestationEnrichingPublicClientAuthenticationConverter(AuthenticationConverter delegate) {
        Assert.notNull(delegate, "delegate must not be null");
        this.delegate = delegate;
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        Authentication result = this.delegate.convert(request);
        if (!(result instanceof OAuth2ClientAuthenticationToken token)
                || !EulerOAuth2ClientAttestationAuthenticationConverter.carriesAttestationSignal(request)) {
            return result;
        }

        if (!(token.getPrincipal() instanceof String clientId)) {
            // A public-client token carries the client_id as its principal. If that is somehow not a
            // String, leave the token untouched rather than risk a ClassCastException rebuilding it.
            return result;
        }

        Map<String, Object> merged = new HashMap<>(token.getAdditionalParameters());
        EulerOAuth2ClientAttestationAuthenticationConverter.collectAttestationParams(request, merged);
        // Keep the request client_id in the parameters too, so the verifier's Section 6.3
        // consistency check can run it against the client_id the attestation resolves to.
        merged.putIfAbsent(OAuth2ParameterNames.CLIENT_ID, clientId);
        return new OAuth2ClientAuthenticationToken(
                clientId, token.getClientAuthenticationMethod(), token.getCredentials(), merged);
    }
}
