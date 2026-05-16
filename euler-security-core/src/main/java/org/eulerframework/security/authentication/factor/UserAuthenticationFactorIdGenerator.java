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
package org.eulerframework.security.authentication.factor;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates opaque, URL-safe identifiers for {@link UserAuthenticationFactor}
 * records.
 * <p>
 * Format: {@code idp_} + 22 base64url characters (16 random bytes), matching
 * the {@code ot_xxxxxxxx} convention of the OTP module so the two id spaces
 * stay visually consistent at the API surface. The {@code idp} mnemonic
 * stands for <em>identity</em> (the public-facing concept exposed by
 * {@code /user/identities}); the underlying domain object is renamed to
 * {@code UserAuthenticationFactor} but the URL and id prefix are kept
 * stable for the v2 contract.
 * <p>
 * Implementations are thread-safe.
 */
public final class UserAuthenticationFactorIdGenerator {

    /**
     * Prefix prepended to every generated id.
     */
    public static final String ID_PREFIX = "idp_";

    private static final int RANDOM_BYTES = 16; // -> 22 base64url chars

    private final SecureRandom secureRandom;

    /**
     * Create a generator using a fresh {@link SecureRandom}.
     */
    public UserAuthenticationFactorIdGenerator() {
        this(new SecureRandom());
    }

    /**
     * Create a generator that draws randomness from the given source.
     * Useful in tests to inject a deterministic random.
     *
     * @param secureRandom the random source; must not be {@code null}
     */
    public UserAuthenticationFactorIdGenerator(SecureRandom secureRandom) {
        if (secureRandom == null) {
            throw new IllegalArgumentException("secureRandom must not be null");
        }
        this.secureRandom = secureRandom;
    }

    /**
     * Mint a fresh, opaque factor id.
     *
     * @return a new id, never {@code null}
     */
    public String generate() {
        byte[] bytes = new byte[RANDOM_BYTES];
        this.secureRandom.nextBytes(bytes);
        String suffix = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return ID_PREFIX + suffix;
    }
}
