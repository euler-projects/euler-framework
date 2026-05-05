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
package org.eulerframework.security.oauth2.server.authorization.jwk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@link JwkRepository} seeded from a caller-provided collection of
 * {@link JwkEntry} instances (typically materialized by the JWK
 * autoconfiguration from the
 * {@code euler.security.oauth2.authorizationserver.jwk.keys} map).
 *
 * <p>Both {@link #load()} and {@link #save(JwkEntry)} are fully supported:
 * {@code save()} is an upsert by {@code kid} so admin-originated lifecycle
 * transitions work even without a persistent backend (within a single JVM).
 *
 * <p>{@link #load()} enforces the cross-entry invariants of
 * {@link JwkManageService}:
 * <ul>
 *   <li>each {@code jwk.algorithm} carries at most one entry with
 *       {@code status=ACTIVE}; multiple {@code ACTIVE} entries across
 *       different algorithms are allowed;</li>
 *   <li>entries with {@code status=ACTIVE} or {@code status=PENDING} MUST
 *       carry a private key;</li>
 *   <li>entries with {@code status=VERIFY_ONLY} / {@code RETIRED} may be
 *       public-only;</li>
 *   <li>{@code kid} uniqueness (structurally guaranteed by the underlying
 *       {@link ConcurrentHashMap}).</li>
 * </ul>
 */
public class InMemoryJwkRepository implements JwkRepository {

    private final ConcurrentHashMap<String, JwkEntry> store = new ConcurrentHashMap<>();

    public InMemoryJwkRepository(Collection<JwkEntry> initial) {
        Objects.requireNonNull(initial, "initial");
        for (JwkEntry entry : initial) {
            Objects.requireNonNull(entry, "initial entry");
            JwkEntry previous = this.store.putIfAbsent(entry.kid(), entry);
            if (previous != null) {
                throw new IllegalStateException("Duplicate kid in initial JWK set: " + entry.kid());
            }
        }
    }

    @Override
    public List<JwkEntry> load() {
        List<JwkEntry> snapshot = new ArrayList<>(this.store.values());
        validateAggregate(snapshot);
        return Collections.unmodifiableList(snapshot);
    }

    @Override
    public JwkEntry save(JwkEntry entry) {
        Objects.requireNonNull(entry, "entry");
        this.store.put(entry.kid(), entry);
        return entry;
    }

    private static void validateAggregate(List<JwkEntry> entries) {
        Map<String, String> activeByAlg = new HashMap<>();
        for (JwkEntry entry : entries) {
            switch (entry.status()) {
                case ACTIVE -> {
                    if (!entry.hasPrivateKey()) {
                        throw new IllegalStateException("Entry " + entry.kid()
                                + ": status=ACTIVE requires a private-key PEM");
                    }
                    String alg = (entry.jwk().getAlgorithm() != null)
                            ? entry.jwk().getAlgorithm().getName() : null;
                    if (alg == null) {
                        throw new IllegalStateException("Entry " + entry.kid()
                                + ": status=ACTIVE requires a non-null algorithm");
                    }
                    String prev = activeByAlg.put(alg, entry.kid());
                    if (prev != null) {
                        throw new IllegalStateException("At most one ACTIVE key per algorithm ("
                                + alg + ") is allowed; found kid=" + prev + " and kid=" + entry.kid());
                    }
                }
                case PENDING -> {
                    if (!entry.hasPrivateKey()) {
                        throw new IllegalStateException("Entry " + entry.kid()
                                + ": status=PENDING requires a private-key PEM so it can be promoted to ACTIVE");
                    }
                }
                case DEPRECATED -> {
                    // Private key retained by operational convention; JwkEntry's
                    // constructor has already logged a WARN if it is missing.
                }
                case VERIFY_ONLY, RETIRED -> {
                    // public-only allowed
                }
            }
        }
    }

    /**
     * Package-private accessor primarily for tests; returns a live view of the
     * backing map. External code should go through {@link #load()}.
     */
    Map<String, JwkEntry> asMap() {
        return this.store;
    }
}
