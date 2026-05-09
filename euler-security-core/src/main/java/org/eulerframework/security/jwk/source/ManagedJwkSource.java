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
import org.eulerframework.security.jwk.JwkStatus;
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
 * <h2>Two projections, one fingerprint</h2>
 * Every reload produces two {@link JWKSet} projections that share the same
 * fingerprint:
 * <ul>
 *     <li><b>publication</b> &mdash; served by this instance itself
 *         (implements {@link JWKSource}); feeds the {@code /oauth2/jwks}
 *         endpoint. Includes every {@link JwkStatus#PENDING},
 *         {@link JwkStatus#ACTIVE}, {@link JwkStatus#DEPRECATED} and
 *         {@link JwkStatus#VERIFY_ONLY} signature key so verifiers can warm up
 *         their caches. Expired and {@link JwkStatus#RETIRED} entries are
 *         dropped. Entries are sorted by {@code kid} for a stable response
 *         body; the ordering has no signing semantics because the signing
 *         path uses the separate {@link #signingJwkSource()} view.</li>
 *     <li><b>signing</b> &mdash; served by {@link #signingJwkSource()}; feeds
 *         the {@code JwtEncoder}. Restricted to
 *         {@link JwkEntry#isActive() active} entries carrying a private key,
 *         so the encoder can never pick up a PENDING / DEPRECATED /
 *         VERIFY_ONLY key or one whose {@code nbf}/{@code exp} window is not
 *         currently open. Bundled repositories enforce "at most one ACTIVE
 *         key per algorithm", which keeps the projection unambiguous per
 *         algorithm; the encoder additionally installs a deterministic
 *         {@code JwkSelector} tie-breaker to stay safe against custom
 *         {@code JwkManageService} implementations that may not carry that
 *         guarantee.</li>
 * </ul>
 */
public final class ManagedJwkSource implements JWKSource<SecurityContext> {
    private final static Logger LOGGER = LoggerFactory.getLogger(ManagedJwkSource.class);

    private final JwkRepository repository;
    private final ReentrantLock reloadLock = new ReentrantLock();

    private volatile LiveState state;

    private final JWKSource<SecurityContext> signingJwkSource = (jwkSelector, context) -> {
        if (this.state == null) {
            this.reload();
        }
        return jwkSelector.select(this.state.signingJwks());
    };

    public ManagedJwkSource(JwkRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) throws KeySourceException {
        if (this.state == null) {
            this.reload();
        }
        return jwkSelector.select(this.state.publicationJwks());
    }

    /**
     * Signing-only view over the same live state, restricted to
     * {@linkplain JwkEntry#isActive() active} entries with a private key.
     * Intended to be injected into {@code NimbusJwtEncoder} so signing can
     * never fall back onto a PENDING / DEPRECATED / VERIFY_ONLY key, nor onto
     * an ACTIVE key whose {@code nbf} has not yet elapsed or whose
     * {@code exp} has already passed.
     *
     * <p>The returned source is backed by the same {@link LiveState} as
     * {@link #get(JWKSelector, SecurityContext)}; both views refresh together
     * on {@link #reload()}.
     *
     * @return a {@link JWKSource} exposing only active signing candidates
     */
    public JWKSource<SecurityContext> signingJwkSource() {
        return this.signingJwkSource;
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

            List<JwkEntry> publication = this.repository.load().stream()
                    .filter(JwkEntry::isSignatureKey)
                    .filter(entry -> !entry.isExpired()) // drop expired / RETIRED entries
                    // Stable response ordering by kid; the ordering carries no
                    // signing semantics because signing is served by the
                    // separate signingJwkSource view.
                    .sorted(Comparator.comparing(JwkEntry::kid))
                    .toList();

            List<JWK> publicationJwks = publication.stream()
                    .map(JwkEntry::jwk)
                    .toList();

            // Signing view: strict lifecycle-based filter (status == ACTIVE and
            // nbf/exp window currently open) plus the private-key requirement.
            // Bundled repositories enforce "at most one ACTIVE key per algorithm",
            // so per-algorithm this list is expected to contain at most one entry;
            // NimbusJwtEncoder carries a deterministic tie-breaker as an extra
            // line of defense against custom JwkManageService implementations.
            List<JWK> signingJwks = publication.stream()
                    .filter(JwkEntry::isActive)
                    .filter(JwkEntry::hasPrivateKey)
                    .map(JwkEntry::jwk)
                    .toList();

            LiveState next = new LiveState(
                    new JWKSet(publicationJwks),
                    new JWKSet(signingJwks),
                    nextFingerprint);

            this.state = next;

            if (LOGGER.isDebugEnabled()) {
                List<String> publicationKids = publicationJwks.stream().map(JWK::getKeyID).toList();
                List<String> signingKids = signingJwks.stream().map(JWK::getKeyID).toList();
                LOGGER.debug("Jwk source reload: fingerprint: {}, publication kids: {}, signing kids: {}",
                        next.fingerprint, publicationKids, signingKids);
            }
        } finally {
            this.reloadLock.unlock();
        }
    }


    record LiveState(@Nonnull JWKSet publicationJwks,
                     @Nonnull JWKSet signingJwks,
                     @Nonnull String fingerprint) {
    }
}
