/*
 * Copyright 2013-2026 the original author or authors.
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

package org.eulerframework.security.authentication.apple;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;

/**
 * An {@link org.springframework.security.core.Authentication} implementation that is
 * designed for Apple App Attest verification.
 */
public class AppleAppAttestAttestationAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;

    private Object credentials;

    private final String keyId;

    private final String challenge;

    /**
     * This constructor can be safely used by any code that wishes to create an
     * unauthenticated <code>AppleAppAttestAttestationAuthenticationToken</code>.
     */
    public AppleAppAttestAttestationAuthenticationToken(String keyId, Object attestation, String challenge) {
        super(Collections.emptyList());
        this.principal = null;
        this.credentials = attestation;
        this.keyId = keyId;
        this.challenge = challenge;
        setAuthenticated(false);
    }

    /**
     * This constructor should only be used by <code>AuthenticationManager</code> or
     * <code>AuthenticationProvider</code> implementations that are satisfied with
     * producing a trusted (i.e. {@link #isAuthenticated()} = <code>true</code>)
     * authentication token.
     */
    public AppleAppAttestAttestationAuthenticationToken(Object principal, Object credentials,
                                                        String keyId, String challenge,
                                                        Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        this.keyId = keyId;
        this.challenge = challenge;
        super.setAuthenticated(true); // must use super, as we override
    }

    /**
     * This factory method can be safely used by any code that wishes to create an
     * unauthenticated <code>AppleAppAttestAttestationAuthenticationToken</code>.
     */
    public static AppleAppAttestAttestationAuthenticationToken unauthenticated(String keyId, Object attestation, String challenge) {
        return new AppleAppAttestAttestationAuthenticationToken(keyId, attestation, challenge);
    }

    /**
     * This factory method can be safely used by any code that wishes to create an
     * authenticated <code>AppleAppAttestAttestationAuthenticationToken</code>.
     */
    public static AppleAppAttestAttestationAuthenticationToken authenticated(Object principal, Object credentials,
                                                                             String keyId, String challenge,
                                                                             Collection<? extends GrantedAuthority> authorities) {
        return new AppleAppAttestAttestationAuthenticationToken(principal, credentials, keyId, challenge, authorities);
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    public String getKeyId() {
        return this.keyId;
    }

    public String getChallenge() {
        return this.challenge;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated,
                "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}
