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

package org.eulerframework.security.authentication.appattest;

import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Utility methods for the Apple App Attest / App Assertion protocol, covering both the
 * low-level SHA-256 digest primitive (used to compute {@code clientDataHash},
 * {@code expectedNonce}, etc.) and the higher-level identifiers derived from an app's
 * {@linkplain RegisteredApp#appId() App ID}:
 * <ul>
 *   <li>The <b>RP ID hash</b> (hex-encoded SHA-256 of the App ID), used as the lookup
 *       key for App Attest;</li>
 *   <li>The <b>STATIC OAuth2 {@code client_id}</b> (base64url-encoded, no padding),
 *       shared by all devices running the same app.</li>
 * </ul>
 * Both representations share the same SHA-256 digest but apply different encodings.
 * <p>
 * Accepting {@link RegisteredApp} (rather than individual fields such as {@code teamId}
 * and {@code bundleId}) keeps this API stable as {@code RegisteredApp} evolves to support
 * non-Apple app sources: any change to the app-identifier composition is encapsulated in
 * {@link RegisteredApp#appId()}.
 *
 * @see RegisteredApp
 */
public final class AppAttestUtils {

    private AppAttestUtils() {
        // utility class
    }

    /**
     * Compute the SHA-256 digest of the given bytes.
     * <p>
     * This is the fundamental hash primitive used throughout the App Attest / App
     * Assertion protocol (for {@code clientDataHash}, {@code expectedNonce}, and
     * {@linkplain #appIdHash(RegisteredApp) App ID hashing}).
     *
     * @param data the input bytes
     * @return the raw 32-byte SHA-256 digest
     * @throws IllegalStateException if the {@code SHA-256} algorithm is not available
     */
    public static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Compute the SHA-256 hash of the given app's {@linkplain RegisteredApp#appId() App ID}.
     *
     * @param app the registered app
     * @return the raw 32-byte SHA-256 digest
     */
    public static byte[] appIdHash(RegisteredApp app) {
        Assert.notNull(app, "app must not be null");
        return sha256(app.appId().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Compute the hex-encoded SHA-256 hash of the given app's
     * {@linkplain RegisteredApp#appId() App ID}.
     * <p>
     * This is the RP ID hash representation used for App Attest lookup.
     *
     * @param app the registered app
     * @return the lowercase hex string (64 characters)
     */
    public static String appIdHashHex(RegisteredApp app) {
        return HexFormat.of().formatHex(appIdHash(app));
    }

    /**
     * Compute the STATIC OAuth2 {@code client_id} for the given app.
     * <p>
     * The client ID is the base64url-encoded (no padding) SHA-256 hash of the
     * {@linkplain RegisteredApp#appId() App ID}. This encoding is URL-safe and
     * deterministic, suitable for use as an OAuth2 client identifier.
     *
     * @param app the registered app
     * @return the base64url-encoded client ID (43 characters)
     */
    public static String staticClientId(RegisteredApp app) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(appIdHash(app));
    }
}
