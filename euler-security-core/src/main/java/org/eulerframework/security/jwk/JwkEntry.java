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

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import jakarta.annotation.Nonnull;
import org.eulerframework.security.util.JwkUtils;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Date;

/**
 * Immutable snapshot of a single JWK managed by the framework.
 *
 * <p>The record bundles the Nimbus {@link JWK} (public and/or private key
 * material), the lifecycle {@link JwkStatus}, and a precomputed
 * {@code fingerprint} used by {@link JwkRepository#fingerprint()} and the
 * cache-invalidation logic in
 * {@link org.eulerframework.security.jwk.source.ManagedJwkSource}. The
 * fingerprint is derived via {@link JwkUtils#fingerprint(JWK, JwkStatus)}
 * and therefore reflects both the key material and the lifecycle state.
 *
 * <p>Invariants enforced by the compact constructor:
 * <ul>
 *     <li>{@code jwk}, {@code status} and {@code fingerprint} are non-{@code null};</li>
 *     <li>the JWK carries a {@code kid}, {@code alg} and {@code iat};</li>
 *     <li>{@link JwkStatus#PENDING} and {@link JwkStatus#ACTIVE} entries
 *         MUST expose the private key half.</li>
 * </ul>
 */
public record JwkEntry(@Nonnull JWK jwk, @Nonnull JwkStatus status, @Nonnull byte[] fingerprint) {

    /**
     * Convenience constructor that derives the fingerprint from
     * {@link JwkUtils#fingerprint(JWK, JwkStatus)}.
     */
    public JwkEntry(@Nonnull JWK jwk, @Nonnull JwkStatus status) {
        this(jwk, status, JwkUtils.fingerprint(jwk, status));
    }

    public JwkEntry {
        Assert.notNull(jwk, "jwk is required");
        Assert.notNull(status, "status is required");
        Assert.notNull(fingerprint, "fingerprint is required");
        Assert.notNull(jwk.getKeyID(), "kid is required");
        Assert.notNull(jwk.getAlgorithm(), "alg is required");
        Assert.notNull(jwk.getIssueTime(), "iat is required");
        if (JwkStatus.PENDING.equals(status) || JwkStatus.ACTIVE.equals(status)) {
            Assert.isTrue(jwk.isPrivate(), () -> "privateKey is required for " + status + " key");
        }
    }

    /** Shortcut for {@code jwk.getKeyID()}. */
    @Nonnull
    public String kid() {
        return this.jwk.getKeyID();
    }

    /** Shortcut for {@code jwk.getIssueTime()} as an {@link Instant}. */
    @Nonnull
    public Instant iat() {
        return this.jwk.getIssueTime().toInstant();
    }

    /** {@code true} when the JWK carries private-key material. */
    public boolean hasPrivateKey() {
        return this.jwk.isPrivate();
    }

    /** {@code true} when {@code use == sig}. */
    public boolean isSignatureKey() {
        return KeyUse.SIGNATURE.equals(this.jwk.getKeyUse());
    }

    /**
     * {@code true} when the entry is still pending: either its status is
     * {@link JwkStatus#PENDING}, or the JWK declares a future
     * {@code nbf} (not-before) claim.
     */
    public boolean isPending() {
        return JwkStatus.PENDING.equals(this.status) ||
                jwk.getNotBeforeTime() != null && jwk.getNotBeforeTime().after(new Date());
    }

    /**
     * {@code true} when the entry should be filtered out of the published
     * JWK set: either its status is {@link JwkStatus#RETIRED}, or the JWK
     * declares an {@code exp} (expiration) claim in the past.
     */
    public boolean isExpired() {
        return JwkStatus.RETIRED.equals(this.status) ||
                jwk.getExpirationTime() != null && jwk.getExpirationTime().before(new Date());
    }

    @Nonnull
    @Override
    public String toString() {
        return "JwkEntry{kid=" + this.kid()
                + ", alg=" + this.jwk.getAlgorithm()
                + ", status=" + this.status
                + ", hasPrivate=" + this.hasPrivateKey() + '}';
    }
}
