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

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.OAuth2ClientRegistration;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientRegistrationAuthenticationToken;
import org.springframework.util.Assert;

import java.util.Collections;

/**
 * Carries an Apple App Attest client registration request: the RFC 7591 client metadata parsed
 * from the request body plus the device assertion proof taken from the request headers.
 * <p>
 * <b>This deliberately does not extend {@link OAuth2ClientRegistrationAuthenticationToken}</b>,
 * even though doing so would look tidier. {@code ProviderManager} records an
 * {@code AuthenticationException} and keeps polling the remaining providers, and Spring's
 * {@code OAuth2ClientRegistrationAuthenticationProvider} declares
 * {@code supports()} over that base type. A subclass would therefore also be offered to it, and
 * since this token carries no principal it would reject the request with a generic
 * {@code invalid_token}, overwriting the specific reason
 * {@link EulerOAuth2AttestationBasedClientRegistrationAuthenticationProvider} already reported.
 * Keeping the types unrelated makes {@code supports()} the sole router between the two providers.
 * <p>
 * The {@code OAuth2ClientRegistrationAuthenticationToken} that the endpoint filter requires is
 * built separately, as the authentication <i>result</i>, by that provider.
 *
 * @see EulerOAuth2AttestationBasedClientRegistrationAuthenticationProvider
 */
public class EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken extends AbstractAuthenticationToken {

    private final OAuth2ClientRegistration clientRegistration;

    private final String keyId;

    private final String challenge;

    private final String assertion;

    /**
     * Constructs an {@code EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken}
     * using the provided parameters.
     * @param clientRegistration the client registration parsed from the request body
     * @param keyId the App Attest KEY identifier, as registered via {@code POST /app_attest/register}
     * @param challenge the one-time challenge the assertion was generated over
     * @param assertion the base64url-encoded App Attest assertion
     */
    public EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken(
            OAuth2ClientRegistration clientRegistration, String keyId, String challenge, String assertion) {
        super(Collections.emptyList());
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        Assert.hasText(keyId, "keyId cannot be empty");
        Assert.hasText(challenge, "challenge cannot be empty");
        Assert.hasText(assertion, "assertion cannot be empty");
        this.clientRegistration = clientRegistration;
        this.keyId = keyId;
        this.challenge = challenge;
        this.assertion = assertion;
        setAuthenticated(false);
    }

    /**
     * Returns the client registration parsed from the request body.
     * @return the client registration
     */
    public OAuth2ClientRegistration getClientRegistration() {
        return this.clientRegistration;
    }

    /**
     * Returns the App Attest KEY identifier the assertion was generated with.
     * @return the KEY identifier
     */
    public String getKeyId() {
        return this.keyId;
    }

    /**
     * Returns the one-time challenge the assertion was generated over.
     * @return the challenge
     */
    public String getChallenge() {
        return this.challenge;
    }

    /**
     * Returns the base64url-encoded App Attest assertion, which is the credential this
     * authentication request carries.
     * @return the assertion
     */
    public String getAssertion() {
        return this.assertion;
    }

    /**
     * There is no principal: unlike the initial access token path nothing is authenticated
     * upstream, the assertion is verified by the provider.
     */
    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public Object getCredentials() {
        return this.assertion;
    }

}
