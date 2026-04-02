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

import java.io.Serializable;
import java.security.PublicKey;

/**
 * Represents a stored Apple App Attest key credential, containing the device public key
 * and the current sign count. Used for subsequent assertion verification after initial
 * attestation registration.
 */
public class AppleAppAttestKeyCredential implements Serializable {

    private final String keyId;
    private final PublicKey publicKey;
    private long signCount;

    public AppleAppAttestKeyCredential(String keyId, PublicKey publicKey, long signCount) {
        this.keyId = keyId;
        this.publicKey = publicKey;
        this.signCount = signCount;
    }

    public String getKeyId() {
        return keyId;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public long getSignCount() {
        return signCount;
    }

    public void setSignCount(long signCount) {
        this.signCount = signCount;
    }
}
