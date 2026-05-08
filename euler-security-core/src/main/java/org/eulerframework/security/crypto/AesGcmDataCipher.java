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
package org.eulerframework.security.crypto;

import org.springframework.util.Assert;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;

/**
 * AES-256-GCM implementation of {@link DataCipher}.
 *
 * <p>Each call to {@link #encrypt(String)} draws a fresh 96-bit IV; the
 * 128-bit GCM authentication tag is appended to the ciphertext (standard JCA
 * output layout). No AAD is used.
 *
 * <p>The cipher does not own key material — it resolves the primary key via
 * {@link KeyRepository#getPrimaryKey(String)} for every {@link #encrypt}
 * call and the per-ciphertext key via {@link KeyRepository#getKey(String,
 * String)} for every {@link #decrypt} call, so key rotation is handled
 * transparently by the surrounding {@code KeyRepository}.
 *
 * <p>The envelope format produced is {@code b64url(header):b64url(iv):b64url(ctWithTag)};
 * the header stamps {@code alg="AES-256-GCM"} and the {@code kid} that was
 * selected at encrypt time.
 */
public final class AesGcmDataCipher implements DataCipher {

    /** Stable algorithm identifier used in envelope headers and registry keys. */
    public static final String ALGORITHM = "AES-256-GCM";

    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int KEY_LENGTH_BYTES = 32;

    private final KeyRepository keys;
    private final SecureRandom secureRandom;

    public AesGcmDataCipher(KeyRepository keys) {
        this(keys, new SecureRandom());
    }

    AesGcmDataCipher(KeyRepository keys, SecureRandom secureRandom) {
        this.keys = Objects.requireNonNull(keys, "keys");
        this.secureRandom = Objects.requireNonNull(secureRandom, "secureRandom");
    }

    @Override
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        KeyEntry entry = this.keys.getPrimaryKey(ALGORITHM);
        assertKeyLength(entry);
        byte[] iv = new byte[IV_LENGTH];
        this.secureRandom.nextBytes(iv);
        byte[] ctWithTag;
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(entry.material(), KEY_ALGORITHM),
                    new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            ctWithTag = cipher.doFinal(EncryptedEnvelopeCodec.toUtf8(plaintext));
        }
        catch (GeneralSecurityException ex) {
            throw new IllegalStateException("AES-256-GCM encryption failed (kid=" + entry.kid() + ")", ex);
        }
        return EncryptedEnvelopeCodec.encode(ALGORITHM, entry.kid(), iv, ctWithTag);
    }

    @Override
    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        EncryptedEnvelopeCodec.Envelope envelope = EncryptedEnvelopeCodec.decode(ciphertext);
        if (!ALGORITHM.equals(envelope.algorithmId())) {
            throw new IllegalStateException(
                    "AES-256-GCM cipher received envelope with alg '" + envelope.algorithmId() + "'");
        }
        Assert.hasText(envelope.kid(), "AES-256-GCM envelope missing kid");
        List<byte[]> bodyParts = envelope.bodyParts();
        if (bodyParts.size() != 2) {
            throw new IllegalStateException(
                    "AES-256-GCM expects 2 body parts [iv, ctWithTag], got " + bodyParts.size());
        }
        byte[] iv = bodyParts.get(0);
        byte[] ctWithTag = bodyParts.get(1);
        if (iv.length != IV_LENGTH) {
            throw new IllegalStateException(
                    "AES-256-GCM invalid IV length: expected " + IV_LENGTH + ", got " + iv.length);
        }
        KeyEntry entry = this.keys.getKey(ALGORITHM, envelope.kid());
        assertKeyLength(entry);
        byte[] plaintext;
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(entry.material(), KEY_ALGORITHM),
                    new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            plaintext = cipher.doFinal(ctWithTag);
        }
        catch (GeneralSecurityException ex) {
            throw new IllegalStateException(
                    "AES-256-GCM decryption failed (kid=" + envelope.kid() + ")", ex);
        }
        return EncryptedEnvelopeCodec.fromUtf8(plaintext);
    }

    private static void assertKeyLength(KeyEntry entry) {
        if (entry.material().length != KEY_LENGTH_BYTES) {
            throw new IllegalStateException("AES-256-GCM expects " + KEY_LENGTH_BYTES
                    + "-byte key material, got " + entry.material().length
                    + " (kid=" + entry.kid() + ")");
        }
    }
}
