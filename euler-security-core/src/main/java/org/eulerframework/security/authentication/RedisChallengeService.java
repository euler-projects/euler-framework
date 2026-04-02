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

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.time.Duration;

/**
 * Redis-backed implementation of {@link ChallengeService}.
 * <p>
 * Challenges are stored as Redis keys with automatic TTL-based expiration.
 * The challenge is bound to a specific client ID embedded in the Redis key.
 * Suitable for clustered deployments where multiple server instances need to
 * share challenge state.
 */
public class RedisChallengeService implements ChallengeService {

    private static final String DEFAULT_KEY_PREFIX = "challenge:";

    private final StringRedisTemplate redisTemplate;
    private final ChallengeGenerator challengeGenerator;
    private final Duration challengeLifetime;

    private String keyPrefix = DEFAULT_KEY_PREFIX;

    /**
     * Create an instance with a 5-minute challenge lifetime and the default
     * {@link Base64UrlChallengeGenerator}.
     *
     * @param redisTemplate the Redis template to use
     */
    public RedisChallengeService(StringRedisTemplate redisTemplate) {
        this(redisTemplate, Duration.ofMinutes(5));
    }

    /**
     * Create an instance with the specified challenge lifetime and the default
     * {@link Base64UrlChallengeGenerator}.
     *
     * @param redisTemplate     the Redis template to use
     * @param challengeLifetime how long a challenge remains valid
     */
    public RedisChallengeService(StringRedisTemplate redisTemplate, Duration challengeLifetime) {
        this(redisTemplate, challengeLifetime, new Base64UrlChallengeGenerator());
    }

    /**
     * Create an instance with the specified challenge lifetime and a custom
     * {@link ChallengeGenerator}.
     *
     * @param redisTemplate      the Redis template to use
     * @param challengeLifetime  how long a challenge remains valid
     * @param challengeGenerator the strategy used to generate challenge strings
     */
    public RedisChallengeService(StringRedisTemplate redisTemplate, Duration challengeLifetime, ChallengeGenerator challengeGenerator) {
        Assert.notNull(redisTemplate, "redisTemplate must not be null");
        Assert.notNull(challengeLifetime, "challengeLifetime must not be null");
        Assert.notNull(challengeGenerator, "challengeGenerator must not be null");
        this.redisTemplate = redisTemplate;
        this.challengeLifetime = challengeLifetime;
        this.challengeGenerator = challengeGenerator;
    }

    @Override
    public GeneratedChallenge generateChallenge(String clientId) {
        Assert.hasText(clientId, "clientId must not be empty");
        String challenge = this.challengeGenerator.generateChallenge();

        this.redisTemplate.opsForValue().set(buildKey(clientId, challenge), "1", this.challengeLifetime);
        return new GeneratedChallenge(challenge);
    }

    @Override
    public boolean consumeChallenge(String challenge, String clientId) {
        Assert.hasText(clientId, "clientId must not be empty");
        Boolean deleted = this.redisTemplate.delete(buildKey(clientId, challenge));
        return Boolean.TRUE.equals(deleted);
    }

    public void setKeyPrefix(String keyPrefix) {
        Assert.hasText(keyPrefix, "keyPrefix must not be empty");
        this.keyPrefix = keyPrefix;
    }

    private String buildKey(String clientId, String challenge) {
        return this.keyPrefix + clientId + ":" + challenge;
    }
}
