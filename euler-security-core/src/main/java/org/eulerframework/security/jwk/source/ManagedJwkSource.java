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
package org.eulerframework.security.jwk.source;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.annotation.Nonnull;
import org.eulerframework.security.jwk.JwkEntry;
import org.eulerframework.security.jwk.JwkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Nimbus {@link JWKSource} that projects a {@link JwkRepository} into a live
 * {@link JWKSet} cached in memory. The cache is refreshed only when
 * {@link #reload()} is invoked (explicitly by the management layer, implicitly
 * on the first {@link #get(JWKSelector, SecurityContext)} call), so callers
 * are responsible for driving reloads when the underlying repository
 * changes.
 *
 * <p>Reloads are idempotent: the repository's
 * {@link JwkRepository#fingerprint() fingerprint} is compared against the
 * current snapshot, and a matching fingerprint short-circuits the rebuild.
 * Concurrent reloads are serialized through a {@link ReentrantLock}.
 *
 * <p>During a rebuild the entries are filtered and ordered so that:
 * <ul>
 *     <li>only signature keys are kept (verification use);</li>
 *     <li>expired / RETIRED entries are dropped;</li>
 *     <li>entries carrying a private key come first (signing candidates),
 *         and within each group newer {@code iat} wins;</li>
 *     <li>PENDING entries are pushed to the tail so they are never picked
 *         for signing while still visible for verification warm-up.</li>
 * </ul>
 */
public final class ManagedJwkSource implements JWKSource<SecurityContext> {
    private final static Logger LOGGER = LoggerFactory.getLogger(ManagedJwkSource.class);

    private final JwkRepository repository;
    private final ReentrantLock reloadLock = new ReentrantLock();

    private volatile LiveState state;

    public ManagedJwkSource(JwkRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) throws KeySourceException {
        if (this.state == null) {
            this.reload();
        }
        return jwkSelector.select(this.state.jwks());
    }

    public void reload() {
        this.reloadLock.lock();
        try {
            String nextFingerprint = this.repository.fingerprint();
            LiveState current = this.state;
            if (current != null && current.fingerprint().equals(nextFingerprint)) {
                LOGGER.debug("Jwk source reload: fingerprint unchanged ({}), no-op", nextFingerprint);
                return;
            }

            List<JWK> jwks = this.repository.load().stream()
                    .filter(JwkEntry::isSignatureKey)
                    .filter(entry -> !entry.isExpired()) // drop expired / RETIRED entries
                    // Put private-key holders first; among them, newer iat first; PENDING tail.
                    .sorted(Comparator
                            .comparing(JwkEntry::hasPrivateKey) // private-key holders last (before reverse)
                            .thenComparing(JwkEntry::iat) // newer iat last (before reverse)
                            .reversed() // reverse: private-key holders first, newer iat first
                            .thenComparing(JwkEntry::isPending) // PENDING pushed to the tail
                    )
                    .map(JwkEntry::jwk)
                    .toList();

            JWKSet jwkSet = new JWKSet(jwks);

            LiveState next = new LiveState(jwkSet, nextFingerprint);

            this.state = next;

            if (LOGGER.isDebugEnabled()) {
                List<String> kids = jwks.stream().map(JWK::getKeyID).toList();
                LOGGER.debug("Jwk source reload: fingerprint: {}, sorted kids: {}", next.fingerprint, kids);
            }
        } finally {
            this.reloadLock.unlock();
        }
    }


    record LiveState(@Nonnull JWKSet jwks, @Nonnull String fingerprint) {
    }
}
