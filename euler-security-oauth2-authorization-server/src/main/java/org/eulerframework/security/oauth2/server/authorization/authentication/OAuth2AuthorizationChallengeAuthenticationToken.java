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
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The authentication token for the Authorization Challenge Endpoint defined by
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-first-party-apps-04.txt">
 * draft-ietf-oauth-first-party-apps-04</a>.
 * <p>
 * It has two states, mirroring how this framework's grant tokens separate the request from the result:
 * <ul>
 *   <li>an <b>unauthenticated request</b>, whose {@linkplain #getPrincipal() principal} is the already
 *       authenticated client (an {@code OAuth2ClientAuthenticationToken} placed on the
 *       {@code SecurityContext} by the preceding client authentication filter), and which carries the
 *       user-level one-time-password request token to complete, the requested scopes and the PKCE
 *       {@code code_challenge} to bind the issued authorization code to; and</li>
 *   <li>an <b>authenticated result</b>, whose principal is the resolved user and which carries the
 *       minted {@code authorization_code}.</li>
 * </ul>
 * The endpoint has no {@code redirect_uri} (the draft's flow is redirect-less), so none is carried.
 *
 * @see OAuth2AuthorizationChallengeAuthenticationProvider
 */
public class OAuth2AuthorizationChallengeAuthenticationToken extends AbstractAuthenticationToken {

    private final Authentication principal;
    private final Authentication otpAuthentication;
    private final RegisteredClient registeredClient;
    private final Set<String> scopes;
    private final String codeChallenge;
    private final String codeChallengeMethod;
    private final String authorizationUri;
    private final String authorizationCode;

    /**
     * Create an unauthenticated authorization challenge request.
     *
     * @param clientPrincipal       the authenticated client principal from the {@code SecurityContext}
     * @param otpAuthentication     the user-level one-time-password request token to authenticate
     * @param authorizationUri      the URI of the authorization challenge endpoint
     * @param scopes                the requested scopes, possibly empty
     * @param codeChallenge         the PKCE {@code code_challenge}, may be {@code null}
     * @param codeChallengeMethod   the PKCE {@code code_challenge_method}, may be {@code null}
     */
    public OAuth2AuthorizationChallengeAuthenticationToken(Authentication clientPrincipal,
                                                           Authentication otpAuthentication,
                                                           String authorizationUri,
                                                           @Nullable Set<String> scopes,
                                                           @Nullable String codeChallenge,
                                                           @Nullable String codeChallengeMethod) {
        super(Collections.emptyList());
        Assert.notNull(clientPrincipal, "clientPrincipal must not be null");
        Assert.notNull(otpAuthentication, "otpAuthentication must not be null");
        Assert.hasText(authorizationUri, "authorizationUri must not be empty");
        this.principal = clientPrincipal;
        this.otpAuthentication = otpAuthentication;
        this.registeredClient = null;
        this.authorizationUri = authorizationUri;
        this.scopes = Collections.unmodifiableSet(
                scopes != null ? new HashSet<>(scopes) : Collections.emptySet());
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
        this.authorizationCode = null;
        setAuthenticated(false);
    }

    /**
     * Create an authenticated authorization challenge result carrying the minted authorization code.
     *
     * @param registeredClient  the client the authorization code was issued to
     * @param principal         the authenticated user principal
     * @param authorizationCode the minted {@code authorization_code}
     */
    public OAuth2AuthorizationChallengeAuthenticationToken(RegisteredClient registeredClient,
                                                           Authentication principal,
                                                           String authorizationCode) {
        super(principal instanceof AbstractAuthenticationToken token
                ? token.getAuthorities()
                : Collections.<GrantedAuthority>emptyList());
        Assert.notNull(registeredClient, "registeredClient must not be null");
        Assert.notNull(principal, "principal must not be null");
        Assert.hasText(authorizationCode, "authorizationCode must not be empty");
        this.principal = principal;
        this.otpAuthentication = null;
        this.registeredClient = registeredClient;
        this.authorizationUri = null;
        this.scopes = Collections.emptySet();
        this.codeChallenge = null;
        this.codeChallengeMethod = null;
        this.authorizationCode = authorizationCode;
        setAuthenticated(true);
    }

    /**
     * The client principal on a request token, or the resolved user principal on a result token.
     */
    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    /**
     * The user-level one-time-password request token to authenticate; {@code null} on a result token.
     */
    @Nullable
    public Authentication getOtpAuthentication() {
        return this.otpAuthentication;
    }

    /**
     * The client the authorization code was issued to; {@code null} on a request token, where the
     * provider resolves it from the {@linkplain #getPrincipal() client principal}.
     */
    @Nullable
    public RegisteredClient getRegisteredClient() {
        return this.registeredClient;
    }

    public Set<String> getScopes() {
        return this.scopes;
    }

    @Nullable
    public String getCodeChallenge() {
        return this.codeChallenge;
    }

    @Nullable
    public String getCodeChallengeMethod() {
        return this.codeChallengeMethod;
    }

    @Nullable
    public String getAuthorizationUri() {
        return this.authorizationUri;
    }

    /**
     * The minted {@code authorization_code}; {@code null} on an unauthenticated request token.
     */
    @Nullable
    public String getAuthorizationCode() {
        return this.authorizationCode;
    }
}
