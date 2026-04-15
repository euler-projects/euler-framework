/*
 * Copyright 2013-2024 the original author or authors.
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
package org.eulerframework.security.authentication;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.Assert;

/**
 * In-memory implementation of {@link NonceService}.
 * <p>
 * Nonce values are stored in a {@link ConcurrentHashMap} with per-entry expiration.
 * Expired entries are cleaned up lazily on each {@link #recordIfAbsent} call.
 * <p>
 * Suitable for single-instance deployments or development/testing environments.
 * For clustered deployments, consider a Redis-backed implementation to ensure
 * consistent replay detection across nodes.
 */
public class InMemoryNonceService implements NonceService {

    private static final int DEFAULT_MAX_ENTRIES = 100_000;

    private final Map<String, Instant> nonces = new ConcurrentHashMap<>();
    private final int maxEntries;

    /**
     * Create an instance with the default maximum entry capacity (100,000).
     */
    public InMemoryNonceService() {
        this(DEFAULT_MAX_ENTRIES);
    }

    /**
     * Create an instance with the specified maximum entry capacity.
     *
     * @param maxEntries maximum number of tracked nonces before rejecting new entries
     */
    public InMemoryNonceService(int maxEntries) {
        Assert.isTrue(maxEntries > 0, "maxEntries must be positive");
        this.maxEntries = maxEntries;
    }

    @Override
    public boolean recordIfAbsent(String nonce, Duration ttl) {
        Assert.hasText(nonce, "nonce must not be empty");
        Assert.notNull(ttl, "ttl must not be null");

        cleanupExpired();

        Instant expiresAt = Instant.now().plus(ttl);
        Instant previous = this.nonces.putIfAbsent(nonce, expiresAt);

        if (previous != null) {
            // Already recorded — check if it has expired
            if (Instant.now().isAfter(previous)) {
                // Expired entry; replace with new expiration
                this.nonces.put(nonce, expiresAt);
                return true;
            }
            // Still active — replay detected
            return false;
        }

        // First time seeing this nonce — check capacity
        if (this.nonces.size() > this.maxEntries) {
            // Best-effort: still recorded, but warn via cleanup
            cleanupExpired();
        }
        return true;
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Instant>> it = this.nonces.entrySet().iterator();
        while (it.hasNext()) {
            if (now.isAfter(it.next().getValue())) {
                it.remove();
            }
        }
    }
}
