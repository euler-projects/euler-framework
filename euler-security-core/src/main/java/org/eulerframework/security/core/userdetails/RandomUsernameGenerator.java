/*
 * Copyright 2013-2026 the original author or authors.
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
package org.eulerframework.security.core.userdetails;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates fixed-length, high-entropy random usernames for users that are
 * provisioned implicitly (e.g. OTP signup, device attestation signup, social
 * login signup) where the upstream identifier (phone number, device key id,
 * WeChat openId, ...) must not leak into the local username.
 * <p>
 * All call sites that need to populate
 * {@link EulerUserDetails.UserBuilder#username(String)} with a randomly
 * generated value MUST use this generator to keep the username scheme
 * consistent across the codebase.
 */
public final class RandomUsernameGenerator {

    /** Prefix that flags an auto-provisioned, opaque username. */
    public static final String USERNAME_PREFIX = "u-";

    /** 12 random bytes -> exactly 16 base64url characters without padding (~96-bit entropy). */
    private static final int RANDOM_BYTES = 12;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private RandomUsernameGenerator() {
        // no instances
    }

    /**
     * Mints a fresh random username of the form {@code u-<base64url16>}:
     * 12 random bytes encoded as 16 URL-safe Base64 characters without
     * padding (~96 bits of entropy), opaque and safe to embed in URLs. The
     * body is guaranteed to start with a base64url alphanumeric character,
     * never {@code '-'} or {@code '_'}.
     *
     * @return the freshly generated username
     */
    public static String generate() {
        byte[] random = new byte[RANDOM_BYTES];
        // The first character encodes the top 6 bits of random[0]
        // ((random[0] & 0xFF) >>> 2); indices 62 and 63 map to '-' and '_'.
        // Reject those draws so the body never starts with a separator-like
        // symbol. Only 2 of 64 leading symbols are excluded (~3%), so the
        // loop terminates almost immediately.
        do {
            SECURE_RANDOM.nextBytes(random);
        } while (((random[0] & 0xFF) >>> 2) >= 62);
        return USERNAME_PREFIX + ENCODER.encodeToString(random);
    }
}
