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

import java.util.List;

/**
 * Service contract for managing {@link JwkEntry} lifecycle.
 *
 * <p>Methods follow strict CRUDL semantics; no upsert is provided. The business
 * primary key is {@link JwkEntry#kid() kid}. Write-side methods additionally
 * trigger {@code ReloadableJwkSource.reload()} and poll for cluster convergence
 * so callers observe a post-propagation state before returning.
 *
 * <h2>Cross-entry validation contract</h2>
 * Implementations MUST enforce the following invariants across the full entry
 * set on every write. Direct implementors bear the full burden; {@link
 * AbstractJwkManageService} already codifies them in
 * {@code validateAggregate(List)}.
 * <ul>
 *   <li>Each {@code jwk.algorithm} may carry at most one
 *       {@link JwkStatus#ACTIVE ACTIVE} entry. Multiple {@code ACTIVE}
 *       entries across <em>different</em> algorithms are allowed.</li>
 *   <li>An {@link JwkStatus#ACTIVE ACTIVE} or {@link JwkStatus#PENDING PENDING}
 *       entry MUST carry a private key.</li>
 *   <li>{@link JwkEntry#kid() kid} values MUST be unique across the entry set.</li>
 *   <li>Per-entry JWK self-description (supported {@code alg}, {@code use=sig},
 *       non-null {@code iat}) is also validated on write.</li>
 * </ul>
 *
 * <h2>Recommendation</h2>
 * Applications SHOULD extend {@link AbstractJwkManageService} and only
 * implement the backend-specific {@code doXxx} hooks. Direct implementation of
 * this interface is supported but requires the application to re-assert the
 * cross-entry validation contract above on every mutation path.
 */
public interface JwkManageService {

    /**
     * Creates a new key entry.
     * <p>
     * Fails when an entry with the same {@link JwkEntry#kid() kid} already
     * exists, or when the incoming entry violates the cross-entry validation
     * contract (see class-level Javadoc).
     *
     * @param entry the entry to create
     * @return the created entry
     */
    JwkEntry createKey(JwkEntry entry);

    /**
     * Loads an entry by its {@link JwkEntry#kid() kid}.
     *
     * @param kid the key identifier
     * @return the entry, or {@code null} if not found
     */
    JwkEntry findByKid(String kid);

    /**
     * Lists every stored entry (any {@link JwkStatus}).
     * <p>
     * No pagination is offered: the JWK set is expected to stay small (a
     * handful of keys per algorithm) so a full scan is always cheap enough.
     * Ordering is unspecified.
     *
     * @return all entries
     */
    List<JwkEntry> listKeys();

    /**
     * Updates an existing entry using full-overwrite (replace) semantics,
     * mirroring HTTP {@code PUT}: every mutable field on {@code entry}
     * replaces the persisted state verbatim. Use {@link #patchKey(JwkEntry)}
     * for partial updates.
     * <p>
     * Fails when no entry with the given {@link JwkEntry#kid() kid} exists, or
     * when the resulting entry set would violate the cross-entry validation
     * contract (see class-level Javadoc).
     *
     * @param entry the entry carrying the updated state
     */
    void updateKey(JwkEntry entry);

    /**
     * Updates an existing entry using patch semantics, mirroring HTTP
     * {@code PATCH}: only fields carrying a non-{@code null} value on
     * {@code entry} are applied; {@code null} fields leave the persisted state
     * unchanged.
     * <p>
     * Because a JWK is immutable by {@code kid} (changing the key material
     * would require issuing a new {@code kid}), in practice only
     * {@link JwkEntry#status() status} can be patched.
     * <p>
     * Fails when no entry with the given {@link JwkEntry#kid() kid} exists, or
     * when the resulting entry set would violate the cross-entry validation
     * contract (see class-level Javadoc).
     *
     * @param entry the entry carrying the fields to patch
     */
    void patchKey(JwkEntry entry);

    /**
     * Deletes an entry by its {@link JwkEntry#kid() kid}.
     * <p>
     * Implementations MUST require the entry to be in
     * {@link JwkStatus#RETIRED RETIRED} before physical removal, preventing
     * accidental deletion of still-published material.
     *
     * @param kid the key identifier
     */
    void deleteByKid(String kid);
}
