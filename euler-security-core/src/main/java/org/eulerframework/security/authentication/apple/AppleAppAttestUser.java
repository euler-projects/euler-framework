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

package org.eulerframework.security.authentication.apple;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Objects;

/**
 * Represents a validated Apple App Attest user identified by their key ID.
 * <p>
 * This object is produced by {@link AppleAppAttestValidationService} after successful
 * attestation validation and is used by the authentication providers
 * to look up or create the corresponding user account.
 */
public class AppleAppAttestUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String keyId;
    private final String teamId;
    private final String bundleId;
    private final PublicKey publicKey;

    public AppleAppAttestUser(String keyId) {
        this(keyId, null, null, null);
    }

    public AppleAppAttestUser(String keyId, PublicKey publicKey) {
        this(keyId, null, null, publicKey);
    }

    public AppleAppAttestUser(String keyId, String teamId, String bundleId, PublicKey publicKey) {
        this.keyId = keyId;
        this.teamId = teamId;
        this.bundleId = bundleId;
        this.publicKey = publicKey;
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

    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppleAppAttestUser that = (AppleAppAttestUser) o;
        return Objects.equals(keyId, that.keyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyId);
    }

    @Override
    public String toString() {
        return "AppleAppAttestUser{keyId='" + keyId + "', teamId='" + teamId + "', bundleId='" + bundleId + "'}";
    }
}
