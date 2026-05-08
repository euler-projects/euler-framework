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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Helper for turning filesystem- or passphrase-based key sources into 32-byte
 * AES-256 material suitable for {@link KeyEntry#material()}.
 *
 * <h2>Sources</h2>
 * <ul>
 *   <li>{@link #loadFromKeyFile(Path)} — production-recommended. The file
 *       MUST contain exactly 32 random bytes and have POSIX permissions
 *       {@code 0600}.</li>
 *   <li>{@link #deriveFromPassphrase(char[], String, String)} — development
 *       only. Derives 32 bytes via PBKDF2-HMAC-SHA256 (600k iterations, salt
 *       derived from a caller-supplied namespace plus {@code kid}).</li>
 * </ul>
 *
 * <p>{@link #load(String, String, String, String, String)} is a convenience
 * that selects between the two according to the supplied configuration inputs.
 */
public final class KeyMaterialLoader {

    private static final Logger log = LoggerFactory.getLogger(KeyMaterialLoader.class);

    /**
     * Default PBKDF2 salt namespace used when callers do not supply one.
     * Applications that must remain bug-compatible with a pre-existing
     * deployment SHOULD pass their historical namespace explicitly.
     */
    public static final String DEFAULT_SALT_NAMESPACE = "euler-data-key/";

    private static final int KEY_LENGTH_BYTES = 32;
    private static final int PBKDF2_ITERATIONS = 600_000;
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";

    private static final Set<PosixFilePermission> ALLOWED_KEY_FILE_PERMS = EnumSet.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE);

    private KeyMaterialLoader() {
    }

    /**
     * Select a source by configuration inputs. {@code keyFile} takes
     * precedence; when blank, falls back to {@code passphrase}. Both blank is
     * a fail-fast error. A blank {@code saltNamespace} falls back to
     * {@link #DEFAULT_SALT_NAMESPACE}; only consulted for the passphrase path.
     */
    public static byte[] load(String alg, String kid, String keyFile, String passphrase, String saltNamespace) {
        Assert.hasText(alg, "alg must not be blank");
        Assert.hasText(kid, "kid must not be blank");
        if (StringUtils.hasText(keyFile)) {
            return loadFromKeyFile(Paths.get(keyFile));
        }
        if (StringUtils.hasText(passphrase)) {
            String effectiveNamespace = StringUtils.hasText(saltNamespace)
                    ? saltNamespace : DEFAULT_SALT_NAMESPACE;
            return deriveFromPassphrase(passphrase.toCharArray(), kid, effectiveNamespace);
        }
        throw new IllegalStateException("No key material source configured for alg '"
                + alg + "', kid '" + kid + "' — provide either 'key-file' or 'passphrase'");
    }

    /**
     * Read a 32-byte binary key from {@code keyFile}. Performs a POSIX
     * permission sanity check (strict {@code 0600}) and immediately wipes the
     * intermediate read buffer.
     */
    public static byte[] loadFromKeyFile(Path keyFile) {
        Assert.notNull(keyFile, "keyFile must not be null");
        if (!Files.exists(keyFile)) {
            throw new IllegalStateException("KEY file does not exist: " + keyFile);
        }
        validatePosixPermissions(keyFile);
        byte[] raw;
        try {
            raw = Files.readAllBytes(keyFile);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Unable to read KEY file: " + keyFile, ex);
        }
        if (raw.length != KEY_LENGTH_BYTES) {
            Arrays.fill(raw, (byte) 0);
            throw new IllegalStateException("KEY file must be exactly " + KEY_LENGTH_BYTES
                    + " bytes, got " + raw.length + " (" + keyFile + ")");
        }
        log.info("KEY material loaded from key file: {}", keyFile);
        return raw;
    }

    /**
     * Derive 32 bytes from a passphrase via PBKDF2-HMAC-SHA256 (600k
     * iterations, salt derived from {@code saltNamespace + kid}). Development
     * only; emits a {@code WARN} on every call.
     */
    public static byte[] deriveFromPassphrase(char[] passphrase, String kid, String saltNamespace) {
        Assert.notNull(passphrase, "passphrase must not be null");
        Assert.hasText(kid, "kid must not be blank");
        Assert.hasText(saltNamespace, "saltNamespace must not be blank");
        if (passphrase.length == 0) {
            throw new IllegalStateException("passphrase must not be empty");
        }
        log.warn("KEY material is derived from a passphrase (kid={}, saltNamespace={}). " +
                "Passphrase mode is for development only and MUST NOT be used in production.",
                kid, saltNamespace);
        byte[] salt = deriveSalt(saltNamespace, kid);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            PBEKeySpec spec = new PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BYTES * 8);
            try {
                return factory.generateSecret(spec).getEncoded();
            }
            finally {
                spec.clearPassword();
            }
        }
        catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to derive KEY material from passphrase", ex);
        }
    }

    private static void validatePosixPermissions(Path keyFile) {
        PosixFileAttributeView view = Files.getFileAttributeView(keyFile, PosixFileAttributeView.class);
        if (view == null) {
            log.warn("Skipping POSIX permission check on non-POSIX filesystem for KEY file: {}", keyFile);
            return;
        }
        Set<PosixFilePermission> actual;
        try {
            actual = view.readAttributes().permissions();
        }
        catch (IOException ex) {
            throw new IllegalStateException("Unable to read POSIX permissions for KEY file: " + keyFile, ex);
        }
        for (PosixFilePermission perm : actual) {
            if (!ALLOWED_KEY_FILE_PERMS.contains(perm)) {
                throw new IllegalStateException("KEY file " + keyFile
                        + " has overly permissive permissions " + actual
                        + "; required \u2264 0600 (rw- --- ---)");
            }
        }
    }

    private static byte[] deriveSalt(String saltNamespace, String kid) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] full = sha256.digest((saltNamespace + kid).getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(full, 16);
        }
        catch (GeneralSecurityException ex) {
            throw new IllegalStateException("SHA-256 unavailable for PBKDF2 salt derivation", ex);
        }
    }
}
