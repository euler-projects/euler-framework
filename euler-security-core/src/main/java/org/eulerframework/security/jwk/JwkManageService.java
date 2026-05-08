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

import java.util.List;

/**
 * Lifecycle-aware facade for the JWK subsystem. Implementations own the
 * persistent store and enforce the allowed {@link JwkStatus} transitions;
 * {@link JwkRepository} implementations that back onto this service
 * translate their upsert contract into strict {@code create} / {@code update}
 * calls here.
 *
 * <p>Implementations MUST be thread-safe. The wider framework assumes
 * {@code getFingerprint()} is cheap enough to be polled by
 * {@link org.eulerframework.security.jwk.source.ManagedJwkSource} before
 * every reload attempt.
 */
public interface JwkManageService {

    /**
     * Create a new JWK from a framework-level {@link JwkEntry}. Convenience
     * overload that wraps the entry into the implementation's
     * {@link ManagedJwk} before delegating to {@link #createJwk(ManagedJwk)}.
     */
    ManagedJwk createJwk(JwkEntry entry);

    /**
     * Persist a brand new {@link ManagedJwk}. The {@code kid} MUST be unique
     * within the store; conflicting {@code kid} values MUST be rejected.
     */
    ManagedJwk createJwk(ManagedJwk managedJwk);

    /**
     * Look up the JWK with the supplied {@code kid}. Returns {@code null}
     * when the entry does not exist.
     */
    ManagedJwk getJwk(String kid);

    /** Return every known JWK, in an unspecified order. */
    List<ManagedJwk> listJwks();

    /** Full-replacement update from a framework-level {@link JwkEntry}. */
    void updateJwk(JwkEntry entry);

    /**
     * Full-replacement update: every mutable field on the supplied
     * {@link ManagedJwk} overwrites the persisted row. Unknown {@code kid}
     * MUST be rejected.
     */
    void updateJwk(ManagedJwk managedJwk);

    /**
     * Partial update: only non-{@code null} fields on the supplied
     * {@link ManagedJwk} are applied. Typically used by admin PATCH
     * endpoints to flip the lifecycle status in isolation.
     */
    void patchJwk(ManagedJwk managedJwk);

    /** Remove the entry identified by {@code kid}; unknown values MUST fail fast. */
    void deleteJwk(String kid);

    /**
     * Stable fingerprint of the current store. Equal fingerprints imply an
     * unchanged {@link ManagedJwkSource} snapshot and let reloads short-
     * circuit the rebuild.
     */
    String getFingerprint();
}
