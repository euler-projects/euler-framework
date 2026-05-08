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
package org.eulerframework.security.crypto;

import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Dispatching {@link DataCipher} modelled after Spring Security's
 * {@code DelegatingPasswordEncoder}.
 *
 * <p>Encryption always delegates to a configured <em>primary</em> cipher.
 * Decryption reads the envelope header to pick the right delegate by
 * algorithm identifier, so historical ciphertexts encrypted under a
 * now-secondary algorithm can still be read.
 *
 * <p>Algorithm identifiers are normalised to upper-case when used as map
 * keys; lookups are therefore case-insensitive (e.g. {@code "aes-256-gcm"}
 * matches {@code "AES-256-GCM"}).
 *
 * <p>{@code null} inputs pass through unchanged in both directions.
 */
public final class DelegatingDataCipher implements DataCipher {

    private final Map<String, DataCipher> ciphersByAlg;
    private final String primaryAlg;
    private final DataCipher primary;

    /**
     * @param primaryAlg algorithm identifier (case-insensitive) of the cipher
     *                   to use for encryption; must be present in
     *                   {@code ciphers}
     * @param ciphers    algorithm-identifier → implementation map; keys are
     *                   normalised to upper-case internally
     */
    public DelegatingDataCipher(String primaryAlg, Map<String, DataCipher> ciphers) {
        Assert.hasText(primaryAlg, "primaryAlg must not be blank");
        Assert.notNull(ciphers, "ciphers must not be null");
        Assert.notEmpty(ciphers, "ciphers must not be empty");
        Map<String, DataCipher> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, DataCipher> e : ciphers.entrySet()) {
            Assert.hasText(e.getKey(), "cipher algorithm id must not be blank");
            Objects.requireNonNull(e.getValue(), "cipher must not be null");
            String key = normalize(e.getKey());
            if (normalized.containsKey(key)) {
                throw new IllegalStateException("Duplicate cipher registration for alg '" + e.getKey() + "'");
            }
            normalized.put(key, e.getValue());
        }
        String canonicalPrimary = normalize(primaryAlg);
        DataCipher resolved = normalized.get(canonicalPrimary);
        if (resolved == null) {
            throw new IllegalStateException("primaryAlg '" + primaryAlg
                    + "' is not registered; known algorithms: " + normalized.keySet());
        }
        this.ciphersByAlg = Map.copyOf(normalized);
        this.primaryAlg = canonicalPrimary;
        this.primary = resolved;
    }

    @Override
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        return this.primary.encrypt(plaintext);
    }

    @Override
    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        EncryptedEnvelopeCodec.Envelope envelope = EncryptedEnvelopeCodec.decode(ciphertext);
        String alg = normalize(envelope.algorithmId());
        DataCipher delegate = this.ciphersByAlg.get(alg);
        if (delegate == null) {
            throw new IllegalStateException("No DataCipher registered for alg '"
                    + envelope.algorithmId() + "'; known algorithms: " + this.ciphersByAlg.keySet());
        }
        return delegate.decrypt(ciphertext);
    }

    /** Normalised primary algorithm identifier (upper-case). */
    public String primaryAlgorithm() {
        return this.primaryAlg;
    }

    private static String normalize(String alg) {
        return alg.toUpperCase();
    }
}
