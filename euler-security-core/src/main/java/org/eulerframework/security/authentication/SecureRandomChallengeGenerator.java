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
package org.eulerframework.security.authentication;

import org.springframework.util.Assert;

import java.security.SecureRandom;

/**
 * {@link ChallengeGenerator} implementation that uses {@link SecureRandom} to generate
 * cryptographically strong random bytes.
 */
public class SecureRandomChallengeGenerator implements ChallengeGenerator {

    private static final int DEFAULT_BYTE_LENGTH = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final int byteLength;

    /**
     * Create an instance that generates 32-byte (256-bit) random challenges.
     */
    public SecureRandomChallengeGenerator() {
        this(DEFAULT_BYTE_LENGTH);
    }

    /**
     * Create an instance that generates random challenges of the specified byte length.
     *
     * @param byteLength the number of random bytes to generate
     */
    public SecureRandomChallengeGenerator(int byteLength) {
        Assert.isTrue(byteLength > 0, "byteLength must be positive");
        this.byteLength = byteLength;
    }

    @Override
    public byte[] generateChallenge() {
        byte[] bytes = new byte[this.byteLength];
        this.secureRandom.nextBytes(bytes);
        return bytes;
    }
}
