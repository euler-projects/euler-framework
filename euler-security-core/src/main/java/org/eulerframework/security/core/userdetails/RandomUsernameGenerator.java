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
 * The output format is hard-coded as
 * {@code user_<base64url12>}: 9 random bytes drawn from a shared
 * {@link SecureRandom}, encoded as a fixed-length 12-character URL-safe
 * Base64 string without padding. This yields ~72 bits of entropy and makes
 * the username opaque, recipient-free and safe to embed in URLs.
 * <p>
 * All call sites that need to populate
 * {@link EulerUserDetails.UserBuilder#username(String)} with a randomly
 * generated value MUST use this generator to keep the username scheme
 * consistent across the codebase.
 */
public final class RandomUsernameGenerator {

    /** Prefix that flags an auto-provisioned, opaque username. */
    public static final String USERNAME_PREFIX = "user_";

    /** 9 random bytes -> exactly 12 base64url characters without padding (~72-bit entropy). */
    private static final int RANDOM_BYTES = 9;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private RandomUsernameGenerator() {
        // no instances
    }

    /**
     * Mints a fresh random username as
     * {@code user_<url-safe base64 of 9 random bytes>}.
     *
     * @return the freshly generated username
     */
    public static String generate() {
        byte[] random = new byte[RANDOM_BYTES];
        SECURE_RANDOM.nextBytes(random);
        return USERNAME_PREFIX + ENCODER.encodeToString(random);
    }
}
