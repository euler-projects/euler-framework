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

import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link AppAttestIssuedKeyService}, backed by a
 * {@link ConcurrentHashMap}. Registered keys are lost on restart.
 * <p>
 * A test double and a development convenience only. Auto-configuration deliberately does
 * <b>not</b> fall back to this when no {@link JdbcAppAttestIssuedKeyService} can be built,
 * unlike {@code RegisteredAppRepository}, whose in-memory form is safe because it is repopulated
 * from configuration properties on every start. What this holds is state accumulated at runtime,
 * and losing it degrades silently rather than visibly: accounts and their
 * {@code public_key} identities live elsewhere and keep working for a login that names its
 * subject, while every subject-less first login stops resolving and every App instance has to
 * re-attest and re-register &mdash; landing on a new {@code client_id}, so the keys already
 * registered under the old issuer are orphaned. Failing to start is the better outcome, so an
 * application that genuinely has no {@code JdbcOperations} must declare its own
 * {@link AppAttestIssuedKeyService} bean and own that consequence.
 *
 * @see AppAttestIssuedKeyService
 * @see JdbcAppAttestIssuedKeyService
 */
public class InMemoryAppAttestIssuedKeyService implements AppAttestIssuedKeyService {

    private final Map<String, AppAttestIssuedKey> keys = new ConcurrentHashMap<>();

    @Override
    public AppAttestIssuedKey saveKey(AppAttestIssuedKey key) {
        Assert.notNull(key, "key must not be null");
        String indexKey = indexKey(key.issuer(), key.keyId());
        // put rather than putIfAbsent: the key ID is the thumbprint of the key material, so a
        // repeated registration is by definition the same key, and the latest description of
        // it is the one worth keeping. Mirrors JdbcAppAttestIssuedKeyService, which updates the
        // row an insert collided with.
        this.keys.put(indexKey, key);
        // Read back rather than return the argument, so both implementations answer with what
        // is registered and a caller cannot tell them apart.
        return this.keys.get(indexKey);
    }

    @Override
    public AppAttestIssuedKey findByIssuerAndKeyId(String issuer, String keyId) {
        return this.keys.get(indexKey(issuer, keyId));
    }

    /**
     * Flatten the composite key into one map key. The separator cannot occur in either
     * part: an issuer is a {@code client_id} and a key ID a Base64URL thumbprint.
     */
    private static String indexKey(String issuer, String keyId) {
        return issuer + '\n' + keyId;
    }
}
