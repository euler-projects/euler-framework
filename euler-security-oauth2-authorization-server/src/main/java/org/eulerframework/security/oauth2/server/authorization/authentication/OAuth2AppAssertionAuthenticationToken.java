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

import jakarta.annotation.Nullable;
import org.eulerframework.security.oauth2.core.EulerAuthorizationGrantType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Unauthenticated grant token for {@code grant_type=urn:ietf:params:oauth:grant-type:app_assertion}.
 * <p>
 * The verified App Attest registration is propagated through the parent
 * {@link OAuth2AuthorizationGrantAuthenticationToken#getAdditionalParameters() additionalParameters}
 * map under the key
 * {@link org.eulerframework.security.oauth2.server.authorization.web.EulerOAuth2AttestationBasedClientAuthenticationFilter#VERIFIED_CLIENT_ATTESTATION_PARAMETER}.
 * For this grant the entry is mandatory: the converter rejects requests that
 * have not been verified by
 * {@link org.eulerframework.security.oauth2.server.authorization.web.EulerOAuth2AttestationBasedClientAuthenticationFilter}.
 */
public class OAuth2AppAssertionAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {
    private final Set<String> scopes;

    public OAuth2AppAssertionAuthenticationToken(
            Authentication clientPrincipal,
            @Nullable Set<String> scopes,
            @Nullable Map<String, Object> additionalParameters) {
        super(EulerAuthorizationGrantType.APP_ASSERTION, clientPrincipal, additionalParameters);
        this.scopes = Collections.unmodifiableSet(
                scopes != null ?
                        new HashSet<>(scopes) :
                        Collections.emptySet());
    }

    public Set<String> getScopes() {
        return scopes;
    }
}
