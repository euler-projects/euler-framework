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
package org.eulerframework.security.jwk;

import com.nimbusds.jose.Algorithm;
import org.eulerframework.security.util.JwkUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-local, thread-safe {@link JwkRepository} backed by a single
 * {@link ConcurrentHashMap}. Intended for standalone deployments and tests
 * where no external persistence is available.
 *
 * <p>The backing map acts as the single source of truth: a write-lock
 * serializes the "at most one ACTIVE key per algorithm" check with the
 * actual mutation, so concurrent writers cannot leave two ACTIVE entries
 * under the same algorithm.
 */
public class InMemoryJwkRepository implements JwkRepository {

    private final Object writeLock = new Object();
    private final ConcurrentHashMap<String, JwkEntry> store = new ConcurrentHashMap<>();

    /**
     * Create a repository pre-populated with the supplied entries. A
     * {@code null} or empty collection yields an empty repository.
     */
    public InMemoryJwkRepository(Collection<JwkEntry> initialEntries) {
        if (!CollectionUtils.isEmpty(initialEntries)) {
            for (JwkEntry jwkEntry : initialEntries) {
                this.save(jwkEntry);
            }
        }
    }

    @Override
    public List<JwkEntry> load() {
        return List.copyOf(this.store.values());
    }

    @Override
    public void save(JwkEntry entry) {
        Assert.notNull(entry, "JWK entry cannot be null");

        if (JwkStatus.PENDING.equals(entry.status()) || JwkStatus.ACTIVE.equals(entry.status())) {
            Assert.isTrue(entry.hasPrivateKey(), "PENDING and ACTIVE keys must carry a private key");
        }

        // The store is the single source of truth; a write-lock makes the
        // "at most one ACTIVE key per algorithm" check atomic with the
        // insertion and prevents stale entries after a status transition.
        synchronized (this.writeLock) {
            if (JwkStatus.ACTIVE.equals(entry.status())) {
                Algorithm alg = entry.jwk().getAlgorithm();
                for (JwkEntry existing : this.store.values()) {
                    if (!existing.kid().equals(entry.kid())
                            && JwkStatus.ACTIVE.equals(existing.status())
                            && alg.equals(existing.jwk().getAlgorithm())) {
                        throw new IllegalArgumentException("At most one ACTIVE key per algorithm is allowed, existing kid: "
                                + existing.kid());
                    }
                }
            }
            this.store.put(entry.kid(), entry);
        }
    }

    @Override
    public String fingerprint() {
        return JwkUtils.hashJwkEntryFingerprints(store.values());
    }
}
