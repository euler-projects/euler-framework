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

/**
 * JWK lifecycle status tracked outside the key material itself.
 *
 * <h2>Legal transitions</h2>
 * <ul>
 *   <li>{@code PENDING} → {@code ACTIVE} (promotion; requires private key)</li>
 *   <li>{@code ACTIVE} → {@code DEPRECATED} (graceful stop-signing)</li>
 *   <li>{@code DEPRECATED} → {@code RETIRED}</li>
 *   <li>{@code VERIFY_ONLY} → {@code RETIRED}</li>
 *   <li>direct admission: {@code VERIFY_ONLY} (external public key) or
 *       {@code PENDING} (locally generated with private key)</li>
 *   <li>forbidden: {@code DEPRECATED} → {@code ACTIVE} (no rollback; generate a new key instead)</li>
 * </ul>
 *
 * <h2>Publication and signing</h2>
 * <ul>
 *   <li>Signing candidate ⇔ {@code status == ACTIVE}. Nimbus {@code JWKSelector} picks
 *       among ACTIVE entries by {@code alg}/{@code kid}/{@code use}; with multiple ACTIVE
 *       entries of the same {@code alg}, the newest {@code issuedAt} wins.</li>
 *   <li>{@code /jwks} publication includes {@code PENDING}, {@code ACTIVE}, {@code DEPRECATED}
 *       and {@code VERIFY_ONLY}; {@code RETIRED} is filtered out.</li>
 *   <li>{@code ACTIVE} entries MUST carry a private key (enforced at {@code JwkEntry}
 *       construction).</li>
 * </ul>
 */
public enum JwkStatus {
    /** Published for pre-warming verifier caches, not yet used for signing. Carries a private key. */
    PENDING,
    /** Signing candidate; {@code JWKSelector} picks the signing key among ACTIVE entries. MUST carry a private key. */
    ACTIVE,
    /** Phased-out: still published for verifying historical tokens, never used for new signatures. Conventionally retains the private key. */
    DEPRECATED,
    /** External or historical verify-only public key. Private key is not required. */
    VERIFY_ONLY,
    /** Archived: kept in the repository for audit, filtered out of the published JWK set. */
    RETIRED
}
