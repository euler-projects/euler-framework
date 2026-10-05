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
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyType;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.security.jwk.JwkEntry;
import org.eulerframework.security.jwk.JwkStatus;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
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

    /**
     * The RFC 7638 JWK Thumbprint of a key, Base64URL-encoded.
     *
     * <p>The thumbprint covers the key's required members only, so it is a stable
     * identifier of the key material itself: it is identical for the public and
     * private variants of a key, and independent of the management-layer fields
     * ({@code kid}, {@code alg}, {@code use}). Callers use it where a key has to be
     * addressable or de-duplicated by its material rather than by a label someone
     * happened to attach to it.
     *
     * @param jwk the key; never {@code null}
     * @return the Base64URL-encoded SHA-256 thumbprint
     */
    public static String computeThumbprint(JWK jwk) {
        Assert.notNull(jwk, "jwk can not be null");
        try {
            return jwk.computeThumbprint().toString();
        } catch (JOSEException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }

    /**
     * Reduce a caller-supplied JWK to its public members and reject key material this
     * framework cannot verify a signature with.
     *
     * <p>Two things are refused here rather than downstream:
     * <ul>
     *   <li><b>private members</b> &mdash; a registration request has no business
     *       carrying them, and persisting them would put secret key material in a
     *       column meant to be readable;</li>
     *   <li><b>symmetric keys</b> ({@code kty=oct}) &mdash; a shared secret is not a
     *       public key, and accepting one would open the classic algorithm-confusion
     *       attack in which an attacker signs with a published "public" key used as an
     *       HMAC secret.</li>
     * </ul>
     *
     * <p>The result is re-parsed from its JSON form so that the returned instance
     * carries no private members at all, and its {@code kid} survives unchanged.
     *
     * @param jwk the key as supplied by a caller; never {@code null}
     * @return the public-only variant, of a key type this framework can verify
     * @throws JOSEException if the key is symmetric, of an unsupported key type, or
     *                       cannot be normalised
     */
    public static JWK toPublicJwk(JWK jwk) throws JOSEException {
        Assert.notNull(jwk, "jwk can not be null");
        if (KeyType.OCT.equals(jwk.getKeyType())) {
            throw new JOSEException("A symmetric key (kty=oct) cannot be registered as a public key");
        }
        if (!isSupportedSignatureKeyType(jwk.getKeyType())) {
            throw new JOSEException("Unsupported JWK key type: " + jwk.getKeyType());
        }
        JWK publicJwk = jwk.toPublicJWK();
        if (publicJwk == null) {
            throw new JOSEException("Cannot derive a public JWK from key type: " + jwk.getKeyType());
        }
        try {
            return JWK.parse(publicJwk.toJSONObject());
        } catch (ParseException e) {
            throw new JOSEException("Cannot normalise the public JWK: " + e.getMessage(), e);
        }
    }

    /**
     * Whether signatures made by this key family can be verified here.
     *
     * @param keyType the JWK {@code kty}; may be {@code null}
     * @return {@code true} for the key types {@link #createJwsVerifier} handles
     */
    public static boolean isSupportedSignatureKeyType(KeyType keyType) {
        return KeyType.EC.equals(keyType) || KeyType.RSA.equals(keyType);
    }

    /**
     * Return a copy of a JWK carrying the given {@code kid}, replacing any value the key
     * already had.
     *
     * <p>Nimbus JWKs are immutable and have no generic builder, so the copy goes through
     * the JSON form; every other member is preserved as-is. Useful where an identifier is
     * assigned by the server rather than chosen by whoever supplied the key &mdash; a
     * caller-chosen {@code kid} cannot be relied on to be unique, whereas a derived one
     * can.
     *
     * @param jwk   the key to copy; never {@code null}
     * @param keyId the {@code kid} to set; must not be empty
     * @return a key equal to {@code jwk} except for its {@code kid}
     */
    public static JWK withKeyId(JWK jwk, String keyId) {
        Assert.notNull(jwk, "jwk can not be null");
        Assert.hasText(keyId, "keyId must not be empty");
        Map<String, Object> members = jwk.toJSONObject();
        members.put("kid", keyId);
        try {
            return JWK.parse(members);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Cannot re-parse the JWK with its key ID: " + e.getMessage(), e);
        }
    }

    /**
     * Create the {@link JWSVerifier} for a JWS header, using the given public JWK.
     *
     * <p>Dispatch is by the key's {@code kty} and never by the header's {@code alg}
     * alone, so a header naming an algorithm the key family cannot make (an
     * {@code HS*} algorithm against an EC key, say) is rejected by the verifier
     * itself rather than quietly interpreted. <b>Adding a key family means adding a
     * branch here</b> plus admitting it in {@link #isSupportedSignatureKeyType}.
     *
     * <p>Deliberately not handled:
     * <ul>
     *   <li>{@code kty=OKP} (Ed25519). iOS Secure Enclave only ever produces EC
     *       P-256 keys, and a Curve25519 key on Apple platforms is an exportable
     *       software key, so it would not carry the non-exportable-private-key
     *       property that registered user keys are trusted for. Nimbus additionally
     *       needs BouncyCastle or Tink on the classpath for it. Admit it here once a
     *       deployment actually wants it.</li>
     *   <li>{@code kty=oct} &mdash; see {@link #toPublicJwk}.</li>
     * </ul>
     *
     * @param header    the JWS header naming the algorithm; never {@code null}
     * @param publicKey the public JWK to verify with; never {@code null}
     * @return a verifier for the header's algorithm
     * @throws JOSEException if the key type is unsupported, the key cannot be read,
     *                       or the algorithm does not match the key family
     */
    public static JWSVerifier createJwsVerifier(JWSHeader header, JWK publicKey) throws JOSEException {
        Assert.notNull(header, "header can not be null");
        Assert.notNull(publicKey, "publicKey can not be null");
        KeyType keyType = publicKey.getKeyType();
        if (KeyType.EC.equals(keyType)) {
            return new ECDSAVerifier(publicKey.toECKey().toECPublicKey());
        }
        if (KeyType.RSA.equals(keyType)) {
            return new RSASSAVerifier(publicKey.toRSAKey().toRSAPublicKey());
        }
        throw new JOSEException("Unsupported JWK key type: " + keyType);
    }
}
