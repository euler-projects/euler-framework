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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link AppleAppAttestKeyCredentialService}.
 * <p>
 * Key credentials are stored in a {@link ConcurrentHashMap}.
 * Suitable for single-instance deployments or development/testing environments.
 */
public class InMemoryAppleAppAttestKeyCredentialService implements AppleAppAttestKeyCredentialService {

    private final Map<String, AppleAppAttestKeyCredential> credentials = new ConcurrentHashMap<>();

    @Override
    public void saveKeyCredential(AppleAppAttestKeyCredential credential) {
        this.credentials.put(credential.getKeyId(), credential);
    }

    @Override
    public AppleAppAttestKeyCredential getKeyCredential(String keyId) {
        return this.credentials.get(keyId);
    }

    @Override
    public void updateSignCount(String keyId, long newSignCount) {
        AppleAppAttestKeyCredential credential = this.credentials.get(keyId);
        if (credential != null && newSignCount > credential.getSignCount()) {
            credential.setSignCount(newSignCount);
        }
    }
}
