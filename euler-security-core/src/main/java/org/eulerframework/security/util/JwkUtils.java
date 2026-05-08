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

package org.eulerframework.security.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.security.jwk.JwkEntry;
import org.eulerframework.security.jwk.JwkStatus;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public final class JwkUtils {
    /**
     * Sentinel value when the JwkEntry list is empty.
     */
    public static final String EMPTY_FINGERPRINT = "0";

    private JwkUtils() {
        // Utility class — not instantiable.
    }

    /**
     * Fold a {@code kid}-ordered list of per-entry fingerprints into a
     * single SHA-256 digest. Consumers (e.g. {@link JwkRepository#fingerprint()})
     * MUST sort by {@code kid} before calling so the output is stable
     * regardless of storage iteration order.
     *
     * @return {@link #EMPTY_FINGERPRINT} when the input is empty; a
     *         Base64URL-encoded SHA-256 digest otherwise
     */
    public static String hashFingerprints(List<byte[]> fingerprintOrderedByKid) {
        if (fingerprintOrderedByKid.isEmpty()) {
            return EMPTY_FINGERPRINT;
        }
        MessageDigest md = getDigest();

        for (byte[] each : fingerprintOrderedByKid) {
            md.update(each);
            md.update((byte) '\n');
        }

        return Base64.getUrlEncoder().withoutPadding().encodeToString(md.digest());
    }

    /**
     * Convenience wrapper around {@link #hashFingerprints(List)} that accepts
     * a raw {@link JwkEntry} collection, sorts it by {@code kid} and extracts
     * each entry's precomputed fingerprint.
     */
    public static String hashJwkEntryFingerprints(Collection<JwkEntry> entries) {
        List<byte[]> fingerprintOrderedByKid = entries.stream()
                .sorted(Comparator.comparing(JwkEntry::kid))
                .map(JwkEntry::fingerprint)
                .toList();

        return hashFingerprints(fingerprintOrderedByKid);
    }

    /**
     * Derive a stable per-entry fingerprint combining the lifecycle
     * {@link JwkStatus} with the RFC 7638 JWK thumbprint plus the
     * management-layer fields ({@code kid}, {@code alg}, {@code use},
     * {@code iat}).
     *
     * <p>The thumbprint is computed over the required JWK members only, so
     * public and private variants of the same key produce identical
     * fingerprints; adding the status and management fields guarantees a
     * fingerprint change whenever any framework-visible attribute flips.
     */
    public static byte[] fingerprint(JWK jwk, JwkStatus status) {
        Assert.notNull(jwk, "jwk can not be null");
        Assert.notNull(status, "status can not be null");
        // RFC 7638 thumbprint is computed over the key's required members only and is
        // identical for public/private variants, so use the JWK directly to also cover oct keys.
        String thumbprint;
        try {
            thumbprint = jwk.computeThumbprint().toString();
        } catch (JOSEException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        String kid = (jwk.getKeyID() == null) ? "" : jwk.getKeyID();
        String alg = (jwk.getAlgorithm() == null) ? "" : jwk.getAlgorithm().getName();
        String use = (jwk.getKeyUse() == null) ? "" : jwk.getKeyUse().getValue();
        // Align with RFC 7517 NumericDate (seconds) instead of Date#getTime (milliseconds).
        String iat = (jwk.getIssueTime() == null) ? "" : String.valueOf(jwk.getIssueTime().getTime() / 1000L);
        String raw = status.name() + "\n" + kid + "\n" + alg + "\n" + use + "\n" + iat + "\n" + thumbprint;
        return getDigest().digest(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static MessageDigest getDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }
}
