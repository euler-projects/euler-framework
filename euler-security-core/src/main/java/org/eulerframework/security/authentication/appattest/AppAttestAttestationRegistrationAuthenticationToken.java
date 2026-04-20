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
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * Authentication token for device attestation registration requests.
 * Contains keyId, attestation data, and challenge.
 */
public class AppAttestAttestationRegistrationAuthenticationToken extends AbstractAuthenticationToken {

    private final String keyId;
    private final String attestation;
    private final String challenge;
    private final Object principal;

    /**
     * Create an unauthenticated token.
     */
    AppAttestAttestationRegistrationAuthenticationToken(String keyId, String attestation, String challenge) {
        super(Collections.emptyList());
        this.keyId = keyId;
        this.attestation = attestation;
        this.challenge = challenge;
        this.principal = null;
        setAuthenticated(false);
    }

    /**
     * Create an authenticated token.
     */
    AppAttestAttestationRegistrationAuthenticationToken(Object principal, String keyId,
                                                        Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.keyId = keyId;
        this.attestation = null;
        this.challenge = null;
        super.setAuthenticated(true);
    }

    /**
     * Creates an unauthenticated token containing the attestation data.
     */
    public static AppAttestAttestationRegistrationAuthenticationToken unauthenticated(String keyId, String attestation, String challenge) {
        return new AppAttestAttestationRegistrationAuthenticationToken(keyId, attestation, challenge);
    }

    /**
     * Creates an authenticated token with the resolved principal and authorities.
     */
    public static AppAttestAttestationRegistrationAuthenticationToken authenticated(Object principal, String keyId,
                                                                                    Collection<? extends GrantedAuthority> authorities) {
        return new AppAttestAttestationRegistrationAuthenticationToken(principal, keyId, authorities);
    }

    @Override
    public Object getCredentials() {
        return this.attestation;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getAttestation() {
        return attestation;
    }

    public String getChallenge() {
        return challenge;
    }
}
