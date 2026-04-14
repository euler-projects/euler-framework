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

import org.eulerframework.security.oauth2.core.EulerAuthorizationGrantType;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OAuth2AppleAppAttestAssertionAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {
    private final String keyId;
    private final String assertion;
    private final String challengeId;
    private final Set<String> scopes;

    public OAuth2AppleAppAttestAssertionAuthenticationToken(
            String keyId,
            String assertion,
            String challengeId,
            Authentication clientPrincipal,
            @Nullable Set<String> scopes,
            @Nullable Map<String, Object> additionalParameters) {
        super(EulerAuthorizationGrantType.APPLE_APP_ATTEST_ASSERTION, clientPrincipal, additionalParameters);
        this.keyId = keyId;
        this.assertion = assertion;
        this.challengeId = challengeId;
        this.scopes = Collections.unmodifiableSet(
                scopes != null ?
                        new HashSet<>(scopes) :
                        Collections.emptySet());
    }

    public String getKeyId() {
        return keyId;
    }

    public String getAssertion() {
        return assertion;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public Set<String> getScopes() {
        return scopes;
    }
}
