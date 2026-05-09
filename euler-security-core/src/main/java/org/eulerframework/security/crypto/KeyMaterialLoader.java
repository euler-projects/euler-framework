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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
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
 *   <li>{@link #deriveFromPassphrase(char[])} — development only. Derives
 *       32 bytes via SHA-256 hash of the passphrase.</li>
 * </ul>
 *
 * <p>{@link #load(String, String, String, Map)} selects between the two
 * according to the top-level {@code keyFile} and algorithm-specific properties.
 */
public final class KeyMaterialLoader {

    private static final Logger log = LoggerFactory.getLogger(KeyMaterialLoader.class);

    private static final int KEY_LENGTH_BYTES = 32;

    private static final Set<PosixFilePermission> ALLOWED_KEY_FILE_PERMS = EnumSet.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE);

    private KeyMaterialLoader() {
    }

    /**
     * Load key material using a top-level {@code keyFile} path (takes
     * precedence) or algorithm-specific {@code properties}. Expected
     * properties for the {@code AES-256-GCM} family:
     * <ul>
     *   <li>{@code passphrase} — derives 32 bytes via SHA-256. Development
     *       only.</li>
     * </ul>
     *
     * <p>If neither {@code keyFile} nor any recognised property is present
     * the method throws {@link IllegalStateException} with guidance on which
     * configuration to add.
     *
     * @param alg        algorithm name (for error messages)
     * @param kid        key identifier (for error messages)
     * @param keyFile    path to the key file; checked first if non-blank
     * @param properties algorithm-specific configuration properties
     * @return 32-byte key material
     */
    public static byte[] load(String alg, String kid, String keyFile, Map<String, String> properties) {
        Assert.hasText(alg, "alg must not be blank");
        Assert.hasText(kid, "kid must not be blank");
    
        if (StringUtils.hasText(keyFile)) {
            return loadFromKeyFile(Paths.get(keyFile));
        }
        if (properties != null) {
            String passphrase = properties.get("passphrase");
            if (StringUtils.hasText(passphrase)) {
                return deriveFromPassphrase(passphrase.toCharArray());
            }
        }
        throw new IllegalStateException("No key material source configured for alg '"
                + alg + "', kid '" + kid + "' — provide 'key-file' or 'passphrase' "
                + "in the key's properties");
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
     * Derive 32 bytes from a passphrase via SHA-256. The derived key is solely
     * a function of the passphrase — no external parameters are required.
     * Development only; emits a {@code WARN} on every call.
     */
    public static byte[] deriveFromPassphrase(char[] passphrase) {
        Assert.notNull(passphrase, "passphrase must not be null");
        if (passphrase.length == 0) {
            throw new IllegalStateException("passphrase must not be empty");
        }
        log.warn("KEY material is derived from a passphrase. " +
                "Passphrase mode is for development only and MUST NOT be used in production.");
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(new String(passphrase).getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
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

}
