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
package org.eulerframework.security.oauth2.server.authorization.jwk.source;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import org.eulerframework.security.oauth2.server.authorization.jwk.JwkEntry;
import org.eulerframework.security.oauth2.server.authorization.jwk.JwkStatus;
import org.eulerframework.security.oauth2.server.authorization.util.JwkUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Package-private immutable working state shared by {@link ReloadableJwkSource}
 * implementations. Wraps {@link JWKSet} plus the aggregate {@code fingerprint}
 * that uniquely identifies the content of the published entries.
 * <p>
 * The {@link #fingerprint} is a deterministic SHA-256 digest over each
 * entry's {@link JwkEntry#contentFingerprint()} in publication order; it drives
 * <em>every</em> idempotency / convergence decision:
 * <ul>
 *   <li>{@code reload()} compares the freshly computed fingerprint against the
 *       previously-committed one; if equal, the reload is a no-op;</li>
 *   <li>the clustered coordinator persists the fingerprint as the
 *       cluster-wide SSOT so peers can detect drift without inspecting
 *       individual entries.</li>
 * </ul>
 * Signing selection remains strictly local: the first entry in {@link #entries}
 * is the signing key (or {@code null} when the repository contains no ACTIVE
 * key at all). {@code signingKid()} is a convenience derived accessor.
 */
record LiveState(String fingerprint, List<JwkEntry> entries, JWKSet publishedSet) {

    LiveState {
        Objects.requireNonNull(fingerprint, "fingerprint");
        entries = List.copyOf(entries);
    }

    /** Signing kid (first entry when present, otherwise {@code null}). */
    String signingKid() {
        if (this.entries.isEmpty()) {
            return null;
        }
        JwkEntry head = this.entries.get(0);
        return head.status() == JwkStatus.ACTIVE ? head.kid() : null;
    }

    /** Ordered list of published kids (signing first, verify-only in issuance order). */
    List<String> publishedKids() {
        List<String> kids = new ArrayList<>(this.publishedSet.getKeys().size());
        for (JWK jwk : this.publishedSet.getKeys()) {
            kids.add(jwk.getKeyID());
        }
        return Collections.unmodifiableList(kids);
    }

    /**
     * Build a {@link LiveState} from the repository snapshot. {@link JwkStatus#RETIRED}
     * entries are filtered out; the {@link JwkStatus#ACTIVE} entry (when present) is
     * hoisted to the head of the list; the remaining published entries follow, ordered
     * by {@link JwkEntry#issuedAt()} descending. A single fingerprint is then aggregated
     * from every retained entry's {@link JwkEntry#contentFingerprint()} in that order.
     */
    static LiveState build(List<JwkEntry> entries) {
        List<JwkEntry> retained = filterRetired(entries);
        JwkEntry signing = chooseSigning(retained);
        List<JwkEntry> ordered = orderForPublish(retained, signing);

        List<JWK> jwks = new ArrayList<>(ordered.size());
        Set<String> seen = new LinkedHashSet<>();
        for (JwkEntry entry : ordered) {
            if (seen.add(entry.kid())) {
                jwks.add(entry.jwk());
            }
        }
        JWKSet publishedSet = new JWKSet(jwks);
        String fingerprint = JwkUtils.fingerprint(ordered);
        return new LiveState(fingerprint, ordered, publishedSet);
    }

    private static List<JwkEntry> filterRetired(List<JwkEntry> entries) {
        List<JwkEntry> retained = new ArrayList<>(entries.size());
        for (JwkEntry entry : entries) {
            if (entry.status() != JwkStatus.RETIRED) {
                retained.add(entry);
            }
        }
        return retained;
    }

    /**
     * Pick the signing entry. Only {@link JwkStatus#ACTIVE} entries are eligible;
     * the aggregate invariants enforced by the repositories guarantee at most one
     * ACTIVE at any time, so in practice this returns either that single entry or
     * {@code null} during the brief window before bootstrap.
     */
    private static JwkEntry chooseSigning(List<JwkEntry> retained) {
        JwkEntry best = null;
        for (JwkEntry entry : retained) {
            if (!entry.isUsableForSigning()) {
                continue;
            }
            if (best == null || entry.issuedAt().isAfter(best.issuedAt())) {
                best = entry;
            }
        }
        return best;
    }

    private static List<JwkEntry> orderForPublish(List<JwkEntry> retained, JwkEntry signing) {
        List<JwkEntry> ordered = new ArrayList<>(retained.size());
        if (signing != null) {
            ordered.add(signing);
        }
        List<JwkEntry> rest = new ArrayList<>(retained.size());
        for (JwkEntry entry : retained) {
            if (signing == null || !Objects.equals(entry.kid(), signing.kid())) {
                rest.add(entry);
            }
        }
        rest.sort(Comparator.comparing(JwkEntry::issuedAt).reversed());
        ordered.addAll(rest);
        return ordered;
    }
}
