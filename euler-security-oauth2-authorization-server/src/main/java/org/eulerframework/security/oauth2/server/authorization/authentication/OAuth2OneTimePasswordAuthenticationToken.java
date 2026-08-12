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

import org.eulerframework.security.authentication.otp.OneTimePasswordAuthenticationToken;
import org.eulerframework.security.oauth2.core.EulerAuthorizationGrantType;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Unauthenticated grant token for {@code grant_type=otp}, carrying the
 * user-level {@link OneTimePasswordAuthenticationToken} built from the
 * submitted {@code otp_ticket} id and {@code otp} value.
 * <p>
 * A verified App Attest registration, when present, is propagated through
 * {@link OAuth2AuthorizationGrantAuthenticationToken#getAdditionalParameters()
 * additionalParameters} and handled by {@code OAuth2OneTimePasswordAuthenticationProvider}.
 */
public class OAuth2OneTimePasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    private final OneTimePasswordAuthenticationToken userPrincipal;
    private final Set<String> scopes;

    public OAuth2OneTimePasswordAuthenticationToken(
            OneTimePasswordAuthenticationToken userPrincipal,
            Authentication clientPrincipal,
            @Nullable Set<String> scopes,
            @Nullable Map<String, Object> additionalParameters) {
        super(EulerAuthorizationGrantType.OTP, clientPrincipal, additionalParameters);
        Assert.notNull(userPrincipal, "userPrincipal must not be null");
        this.userPrincipal = userPrincipal;
        this.scopes = Collections.unmodifiableSet(
                scopes != null ?
                        new HashSet<>(scopes) :
                        Collections.emptySet());
    }

    public OneTimePasswordAuthenticationToken getUserPrincipal() {
        return userPrincipal;
    }

    public Set<String> getScopes() {
        return scopes;
    }
}
