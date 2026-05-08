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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * In-memory {@link KeyRepository} built via {@link #builder()}. This class
 * does not read any configuration on its own; configuration wiring is the
 * responsibility of the caller (typically a Spring {@code @Configuration}).
 *
 * <p>Each registered algorithm MUST have at least one key and a primary
 * {@code kid} that resolves to one of its registered keys. Algorithms without
 * keys (e.g. {@code noop}) do not live in this repository.
 */
public final class InMemoryKeyRepository implements KeyRepository {

    private final Map<String, Map<String, KeyEntry>> byAlgThenKid;
    private final Map<String, KeyEntry> primaryByAlg;

    private InMemoryKeyRepository(Map<String, Map<String, KeyEntry>> byAlgThenKid,
                                  Map<String, KeyEntry> primaryByAlg) {
        this.byAlgThenKid = byAlgThenKid;
        this.primaryByAlg = primaryByAlg;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public KeyEntry getPrimaryKey(String alg) {
        Assert.hasText(alg, "alg must not be blank");
        KeyEntry primary = this.primaryByAlg.get(normalize(alg));
        if (primary == null) {
            throw new IllegalStateException("No keys registered for algorithm '" + alg + "'");
        }
        return primary;
    }

    @Override
    public KeyEntry getKey(String alg, String kid) {
        Assert.hasText(alg, "alg must not be blank");
        Assert.hasText(kid, "kid must not be blank");
        Map<String, KeyEntry> keys = this.byAlgThenKid.get(normalize(alg));
        if (keys == null) {
            throw new IllegalStateException("No keys registered for algorithm '" + alg + "'");
        }
        KeyEntry entry = keys.get(kid);
        if (entry == null) {
            throw new IllegalStateException("No key registered for algorithm '"
                    + alg + "' with kid '" + kid + "'");
        }
        return entry;
    }

    @Override
    public boolean supports(String alg) {
        if (alg == null || alg.isEmpty()) {
            return false;
        }
        return this.byAlgThenKid.containsKey(normalize(alg));
    }

    @Override
    public Set<String> algorithms() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(this.byAlgThenKid.keySet()));
    }

    private static String normalize(String alg) {
        return alg.toUpperCase();
    }

    /** Builder for {@link InMemoryKeyRepository}. */
    public static final class Builder {

        private final Map<String, Map<String, KeyEntry>> byAlgThenKid = new LinkedHashMap<>();
        private final Map<String, String> primaryKidByAlg = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Records the primary {@code kid} for {@code alg}. Can be called before
         * or after {@link #addKey(String, String, byte[])}; the final value is
         * validated by {@link #build()}.
         */
        public Builder primaryKid(String alg, String kid) {
            Assert.hasText(alg, "alg must not be blank");
            Assert.hasText(kid, "primaryKid must not be blank");
            this.primaryKidByAlg.put(normalize(alg), kid);
            return this;
        }

        /** Registers a single {@code (alg, kid)} key. Duplicate keys fail fast. */
        public Builder addKey(String alg, String kid, byte[] material) {
            Assert.hasText(alg, "alg must not be blank");
            Assert.hasText(kid, "kid must not be blank");
            Assert.notNull(material, "material must not be null");
            String canonicalAlg = normalize(alg);
            Map<String, KeyEntry> keys = this.byAlgThenKid.computeIfAbsent(canonicalAlg,
                    ignored -> new LinkedHashMap<>());
            if (keys.containsKey(kid)) {
                throw new IllegalStateException("Duplicate key registration for alg '"
                        + alg + "', kid '" + kid + "'");
            }
            keys.put(kid, new KeyEntry(canonicalAlg, kid, material));
            return this;
        }

        public InMemoryKeyRepository build() {
            Map<String, Map<String, KeyEntry>> frozenAlg = new LinkedHashMap<>();
            Map<String, KeyEntry> primaryByAlg = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, KeyEntry>> e : this.byAlgThenKid.entrySet()) {
                String alg = e.getKey();
                Map<String, KeyEntry> keys = e.getValue();
                if (keys.isEmpty()) {
                    throw new IllegalStateException("Algorithm '" + alg + "' has no keys");
                }
                String primaryKid = this.primaryKidByAlg.get(alg);
                if (primaryKid == null || primaryKid.isEmpty()) {
                    throw new IllegalStateException("Algorithm '" + alg + "' is missing primaryKid");
                }
                KeyEntry primary = keys.get(primaryKid);
                if (primary == null) {
                    throw new IllegalStateException("Algorithm '" + alg + "' primaryKid '"
                            + primaryKid + "' does not match any registered kid; known kids: "
                            + keys.keySet());
                }
                frozenAlg.put(alg, Map.copyOf(keys));
                primaryByAlg.put(alg, primary);
            }
            return new InMemoryKeyRepository(Map.copyOf(frozenAlg), Map.copyOf(primaryByAlg));
        }
    }
}
