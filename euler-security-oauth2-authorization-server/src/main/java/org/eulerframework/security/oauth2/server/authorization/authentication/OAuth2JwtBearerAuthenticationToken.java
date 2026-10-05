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

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Unauthenticated grant token for
 * {@code grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer} (RFC 7523), carrying the
 * submitted {@code assertion}.
 * <p>
 * The assertion is deliberately not parsed here: which claims it must carry, and against
 * which key its signature verifies, depend on the issuer named inside it, and only the
 * provider has the trust anchors to answer that.
 *
 * @see OAuth2JwtBearerAuthenticationProvider
 */
public class OAuth2JwtBearerAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    private final String assertion;
    private final Set<String> scopes;

    public OAuth2JwtBearerAuthenticationToken(String assertion,
                                              Authentication clientPrincipal,
                                              @Nullable Set<String> scopes,
                                              @Nullable Map<String, Object> additionalParameters) {
        super(AuthorizationGrantType.JWT_BEARER, clientPrincipal, additionalParameters);
        Assert.hasText(assertion, "assertion must not be empty");
        this.assertion = assertion;
        this.scopes = Collections.unmodifiableSet(
                scopes != null ?
                        new HashSet<>(scopes) :
                        Collections.emptySet());
    }

    /**
     * The RFC 7523 {@code assertion} parameter: a JWS whose payload names the issuer, and
     * optionally the subject, this request authenticates.
     */
    public String getAssertion() {
        return this.assertion;
    }

    @Override
    public Object getCredentials() {
        return this.assertion;
    }

    public Set<String> getScopes() {
        return this.scopes;
    }
}
