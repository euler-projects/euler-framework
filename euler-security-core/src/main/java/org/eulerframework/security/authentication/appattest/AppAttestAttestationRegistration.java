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

import java.io.Serializable;
import java.security.PublicKey;

/**
 * Represents a registered device attestation, containing the flattened attestation
 * data, public key, JWKSet, and current sign count.
 * <p>
 * Created during the attestation (device registration) flow and used during assertion
 * (device re-authentication) to verify the device's identity.
 * <p>
 * All attestation-specific fields are stored as primitive/JDK types for JDBC persistence
 * friendliness, avoiding direct coupling to any third-party validation library.
 *
 * @see AppAttestAttestationRegistrationService
 */
public class AppAttestAttestationRegistration implements Serializable {

    private final String keyId;
    private final String teamId;
    private final String bundleId;

    // Flattened from AttestedCredentialData
    private final byte[] aaguid;
    private final byte[] credentialId;

    // Flattened from AppleAppAttestAttestationStatement
    private final byte[] attestationCertificateChain;
    private final byte[] receipt;

    private final PublicKey publicKey;
    private final String jwks;
    private long signCount;

    public AppAttestAttestationRegistration(String keyId, String teamId, String bundleId,
                                            byte[] aaguid, byte[] credentialId,
                                            byte[] attestationCertificateChain, byte[] receipt,
                                            PublicKey publicKey, String jwks, long signCount) {
        this.keyId = keyId;
        this.teamId = teamId;
        this.bundleId = bundleId;
        this.aaguid = aaguid;
        this.credentialId = credentialId;
        this.attestationCertificateChain = attestationCertificateChain;
        this.receipt = receipt;
        this.publicKey = publicKey;
        this.jwks = jwks;
        this.signCount = signCount;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getBundleId() {
        return bundleId;
    }

    public byte[] getAaguid() {
        return aaguid;
    }

    public byte[] getCredentialId() {
        return credentialId;
    }

    public byte[] getAttestationCertificateChain() {
        return attestationCertificateChain;
    }

    public byte[] getReceipt() {
        return receipt;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getJwks() {
        return jwks;
    }

    public long getSignCount() {
        return signCount;
    }

    public void setSignCount(long signCount) {
        this.signCount = signCount;
    }
}
