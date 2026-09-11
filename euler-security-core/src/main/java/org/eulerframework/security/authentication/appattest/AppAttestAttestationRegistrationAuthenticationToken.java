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
 * Authentication token for the device attestation registration endpoint.
 * <p>
 * This token represents a <b>device/KEY</b> subject, not a user. The
 * unauthenticated form carries the raw registration request ({@code attestation},
 * {@code challenge}); the authenticated form carries the verified
 * {@link AppAttestAttestationRegistration} as its principal and holds no authorities,
 * because device registration establishes no user login state. The key ID is derived
 * from the attestation, so it is not carried on the unauthenticated form.
 */
public class AppAttestAttestationRegistrationAuthenticationToken extends AbstractAuthenticationToken {

    private final String attestation;
    private final String challenge;
    private final Object principal;

    /**
     * Create an unauthenticated token from the registration request. The key ID is not
     * carried here; it is derived from the attestation during validation.
     */
    AppAttestAttestationRegistrationAuthenticationToken(String attestation, String challenge) {
        super(Collections.emptyList());
        this.attestation = attestation;
        this.challenge = challenge;
        this.principal = null;
        setAuthenticated(false);
    }

    /**
     * Create an authenticated token whose principal is the verified device
     * registration. Carries no authorities.
     */
    AppAttestAttestationRegistrationAuthenticationToken(AppAttestAttestationRegistration registration) {
        super(Collections.emptyList());
        this.principal = registration;
        this.attestation = null;
        this.challenge = null;
        super.setAuthenticated(true);
    }

    /**
     * Creates an unauthenticated token containing the attestation data.
     */
    public static AppAttestAttestationRegistrationAuthenticationToken unauthenticated(String attestation, String challenge) {
        return new AppAttestAttestationRegistrationAuthenticationToken(attestation, challenge);
    }

    /**
     * Creates an authenticated token carrying the verified device registration.
     *
     * @param registration the verified attestation registration (device subject)
     * @return an authenticated token with no authorities
     */
    public static AppAttestAttestationRegistrationAuthenticationToken registered(AppAttestAttestationRegistration registration) {
        return new AppAttestAttestationRegistrationAuthenticationToken(registration);
    }

    @Override
    public Object getCredentials() {
        return this.attestation;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    /**
     * Returns the registered key ID derived from the verified registration, or
     * {@code null} for an unauthenticated token.
     */
    public String getKeyId() {
        return this.principal instanceof AppAttestAttestationRegistration registration
                ? registration.getKeyId() : null;
    }

    public String getAttestation() {
        return attestation;
    }

    public String getChallenge() {
        return challenge;
    }
}
