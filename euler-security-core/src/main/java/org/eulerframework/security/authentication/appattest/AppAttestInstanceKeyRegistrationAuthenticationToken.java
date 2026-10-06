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

package org.eulerframework.security.authentication.appattest;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * Authentication token for the App instance key registration endpoint.
 * <p>
 * Like {@link AppAttestAttestationRegistrationAuthenticationToken} this represents an
 * <b>App instance</b> subject, not a user: registering a key it generated creates no account
 * and establishes no login state. The unauthenticated form carries the App Attest assertion
 * credential that authenticates the caller ({@code keyId}, {@code challenge}, {@code assertion})
 * together with the public key it wants registered; the authenticated form carries the stored
 * {@link AppAttestInstanceKeyRegistration} as its principal and holds no authorities.
 * <p>
 * The public key travels as the raw JSON the caller submitted and is parsed and validated by the
 * provider, which is where the JWK machinery lives. Its {@code kid} is <em>not</em> taken from the
 * request: the server derives it from the key material, which is what makes a repeated
 * registration idempotent.
 */
public class AppAttestInstanceKeyRegistrationAuthenticationToken extends AbstractAuthenticationToken {

    private final String keyId;
    private final String challenge;
    private final String assertion;
    private final String publicKeyJson;
    private final Object principal;

    /**
     * Create an unauthenticated token from a registration request.
     *
     * @param keyId         the App Attest KEY identifier the assertion was made with; an
     *                      assertion does not embed it, so the caller has to supply it
     * @param challenge     the one-time challenge the assertion was made over
     * @param assertion     the Base64-encoded App Attest assertion
     * @param publicKeyJson the public JWK to register, as submitted (JSON)
     */
    AppAttestInstanceKeyRegistrationAuthenticationToken(String keyId, String challenge, String assertion,
                                                        String publicKeyJson) {
        super(Collections.emptyList());
        this.keyId = keyId;
        this.challenge = challenge;
        this.assertion = assertion;
        this.publicKeyJson = publicKeyJson;
        this.principal = null;
        setAuthenticated(false);
    }

    /**
     * Create an authenticated token whose principal is the key registration. Carries no
     * authorities.
     */
    AppAttestInstanceKeyRegistrationAuthenticationToken(AppAttestInstanceKeyRegistration registration) {
        super(Collections.emptyList());
        this.principal = registration;
        this.keyId = null;
        this.challenge = null;
        this.assertion = null;
        this.publicKeyJson = null;
        super.setAuthenticated(true);
    }

    /**
     * Creates an unauthenticated token carrying the assertion credential and the public
     * key to register.
     */
    public static AppAttestInstanceKeyRegistrationAuthenticationToken unauthenticated(
            String keyId, String challenge, String assertion, String publicKeyJson) {
        return new AppAttestInstanceKeyRegistrationAuthenticationToken(keyId, challenge, assertion, publicKeyJson);
    }

    /**
     * Creates an authenticated token carrying the key registration as stored.
     *
     * @param registration the registration as stored, its key carrying the server-derived
     *                     {@code kid}
     * @return an authenticated token with no authorities
     */
    public static AppAttestInstanceKeyRegistrationAuthenticationToken registered(
            AppAttestInstanceKeyRegistration registration) {
        return new AppAttestInstanceKeyRegistrationAuthenticationToken(registration);
    }

    @Override
    public Object getCredentials() {
        return this.assertion;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    /**
     * The App Attest KEY identifier the request authenticated with, or {@code null} for an
     * authenticated token. This is the {@code appAttestKid} of the key being registered, not
     * the {@code jwkKid} the server derives from the submitted JWK.
     */
    public String getKeyId() {
        return this.keyId;
    }

    public String getChallenge() {
        return this.challenge;
    }

    public String getAssertion() {
        return this.assertion;
    }

    /**
     * The public JWK to register as submitted (JSON), or {@code null} for an authenticated
     * token.
     */
    public String getPublicKeyJson() {
        return this.publicKeyJson;
    }

    /**
     * The stored registration, or {@code null} for an unauthenticated token.
     */
    public AppAttestInstanceKeyRegistration getRegistration() {
        return this.principal instanceof AppAttestInstanceKeyRegistration registration ? registration : null;
    }
}
