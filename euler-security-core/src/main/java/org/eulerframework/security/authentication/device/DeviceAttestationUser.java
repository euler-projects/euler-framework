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

package org.eulerframework.security.authentication.device;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Objects;

/**
 * Represents a validated device anonymous user identified by their key ID.
 */
public class DeviceAttestationUser implements Serializable {

    private final String keyId;
    private final String teamId;
    private final String bundleId;
    private final PublicKey publicKey;

    public DeviceAttestationUser(String keyId) {
        this(keyId, null, null, null);
    }

    public DeviceAttestationUser(String keyId, PublicKey publicKey) {
        this(keyId, null, null, publicKey);
    }

    public DeviceAttestationUser(String keyId, String teamId, String bundleId, PublicKey publicKey) {
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
        DeviceAttestationUser that = (DeviceAttestationUser) o;
        return Objects.equals(keyId, that.keyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyId);
    }

    @Override
    public String toString() {
        return "DeviceAttestationUser{keyId='" + keyId + "', teamId='" + teamId + "', bundleId='" + bundleId + "'}";
    }
}
