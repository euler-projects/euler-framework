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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

/**
 * Immutable single JWK record carrying a lifecycle {@link JwkStatus}.
 * The underlying {@link JWK} MUST contain an {@code iat} custom parameter (seconds since epoch).
 *
 * <h2>Invariants enforced at construction</h2>
 * <ul>
 *   <li>{@code status == ACTIVE} ⇒ {@link JWK#isPrivate()} MUST be true; otherwise
 *       {@link IllegalArgumentException} is thrown.</li>
 *   <li>{@code PENDING} / {@code DEPRECATED} conventionally retain a private key but the
 *       constructor only warns when it is missing instead of failing hard (operational
 *       latitude: an operator may intentionally expose a former signing key as
 *       verify-only before the final retire step).</li>
 *   <li>{@code VERIFY_ONLY} imposes no private-key requirement.</li>
 * </ul>
 */
public final class JwkEntry {

    private static final Logger log = LoggerFactory.getLogger(JwkEntry.class);

    private final JWK jwk;
    private final JwkStatus status;

    /** Lazy-cached RFC 7638-based aggregate fingerprint; computed once per instance. */
    private volatile String contentFingerprint;

    public JwkEntry(JWK jwk, JwkStatus status) {
        this.jwk = Objects.requireNonNull(jwk, "jwk");
        this.status = Objects.requireNonNull(status, "status");
        if (status == JwkStatus.ACTIVE && !this.jwk.isPrivate()) {
            throw new IllegalArgumentException("JWK kid=" + this.jwk.getKeyID()
                    + ": status=ACTIVE requires a private key");
        }
        if ((status == JwkStatus.PENDING || status == JwkStatus.DEPRECATED) && !this.jwk.isPrivate()) {
            log.warn("JWK kid={} status={} has no private key; this is unusual for {} "
                    + "and should only happen when demoting an external key",
                    this.jwk.getKeyID(), status, status);
        }
    }

    public JWK jwk() {
        return this.jwk;
    }

    public JwkStatus status() {
        return this.status;
    }

    public String kid() {
        return this.jwk.getKeyID();
    }

    public boolean hasPrivateKey() {
        return this.jwk.isPrivate();
    }

    /**
     * True when the entry participates in the signing candidate pool, i.e.
     * {@code status == ACTIVE}. Combined with the ACTIVE-implies-private-key
     * invariant enforced at construction, a {@code true} return value also implies
     * {@link #hasPrivateKey()}.
     */
    public boolean isUsableForSigning() {
        return this.status == JwkStatus.ACTIVE;
    }

    /**
     * Reads the {@code iat} custom parameter from the underlying JWK.
     *
     * @throws IllegalStateException when {@code iat} is missing or malformed.
     */
    public Instant issuedAt() {
        Object iat = this.jwk.toJSONObject().get("iat");
        if (iat instanceof Number n) {
            return Instant.ofEpochSecond(n.longValue());
        }
        throw new IllegalStateException("JWK " + this.jwk.getKeyID() + " missing required 'iat'");
    }

    /**
     * Deterministic fingerprint of the entry's business-relevant content, used by
     * {@code LiveState} to aggregate a cluster-level fingerprint for cache alignment.
     *
     * <p>Inputs:
     * <ul>
     *   <li>{@code kid} / {@link #status()} / {@code alg} / {@code use} /
     *       {@link #issuedAt()} serialised as ISO-8601 string.</li>
     *   <li>The public RFC 7638 JWK thumbprint
     *       ({@code jwk.toPublicJWK().computeThumbprint()}): its kty-specific canonical
     *       form is standardised and contains only public key parameters, so it is
     *       stable across field-order variations and never exposes private material.</li>
     * </ul>
     *
     * <p>Excluded on purpose:
     * <ul>
     *   <li>private key material (would defeat the entire purpose of using a public
     *       thumbprint);</li>
     *   <li>the raw {@code jwk.toJSONString()} (JSON field ordering is unstable across
     *       JDKs / Nimbus versions);</li>
     *   <li>any ambient clock or cache metadata.</li>
     * </ul>
     *
     * <p>The result is a Base64Url-encoded SHA-256 digest. The instance is immutable
     * so the value is computed lazily on first call and cached; subsequent calls
     * return the cached string.
     */
    public String contentFingerprint() {
        String cached = this.contentFingerprint;
        if (cached != null) {
            return cached;
        }
        String thumbprint;
        try {
            thumbprint = this.jwk.toPublicJWK().computeThumbprint().toString();
        }
        catch (JOSEException ex) {
            throw new IllegalStateException("Failed to compute RFC 7638 JWK thumbprint for kid="
                    + this.jwk.getKeyID(), ex);
        }
        String alg = (this.jwk.getAlgorithm() == null) ? "" : this.jwk.getAlgorithm().getName();
        String use = (this.jwk.getKeyUse() == null) ? "" : this.jwk.getKeyUse().getValue();
        String raw = kid() + "|" + this.status.name() + "|" + alg + "|" + use + "|"
                + issuedAt().toString() + "|" + thumbprint;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable in the JRE", ex);
        }
        byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
        cached = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        this.contentFingerprint = cached;
        return cached;
    }

    @Override
    public String toString() {
        return "JwkEntry{kid=" + this.kid()
                + ", alg=" + this.jwk.getAlgorithm()
                + ", status=" + this.status
                + ", hasPrivate=" + this.hasPrivateKey() + '}';
    }
}
