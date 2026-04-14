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
package org.eulerframework.security.authentication;

import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link ChallengeService}.
 * <p>
 * Challenges are stored in a {@link ConcurrentHashMap} with automatic expiration.
 * Expired entries are cleaned up lazily on each {@link #generateChallenge()} call.
 * <p>
 * Suitable for single-instance deployments or development/testing environments.
 * For clustered deployments, use {@link RedisChallengeService} or
 * {@link JdbcChallengeService} instead.
 */
public class InMemoryChallengeService implements ChallengeService {

    private static final int DEFAULT_MAX_CHALLENGES = 10000;

    private final Map<String, ChallengeEntry> challenges = new ConcurrentHashMap<>();
    private final ChallengeGenerator challengeGenerator;
    private final Duration challengeLifetime;
    private final int maxChallenges;

    /**
     * Create an instance with a 5-minute challenge lifetime, default max capacity,
     * and the default {@link Base64UrlChallengeGenerator}.
     */
    public InMemoryChallengeService() {
        this(Duration.ofMinutes(5));
    }

    /**
     * Create an instance with the specified challenge lifetime, default max capacity,
     * and the default {@link Base64UrlChallengeGenerator}.
     *
     * @param challengeLifetime how long a challenge remains valid
     */
    public InMemoryChallengeService(Duration challengeLifetime) {
        this(challengeLifetime, DEFAULT_MAX_CHALLENGES);
    }

    /**
     * Create an instance with the specified challenge lifetime, max capacity,
     * and the default {@link Base64UrlChallengeGenerator}.
     *
     * @param challengeLifetime how long a challenge remains valid
     * @param maxChallenges     maximum number of active challenges allowed
     */
    public InMemoryChallengeService(Duration challengeLifetime, int maxChallenges) {
        this(challengeLifetime, maxChallenges, new Base64UrlChallengeGenerator());
    }

    /**
     * Create an instance with the specified challenge lifetime, max capacity,
     * and a custom {@link ChallengeGenerator}.
     *
     * @param challengeLifetime  how long a challenge remains valid
     * @param maxChallenges      maximum number of active challenges allowed
     * @param challengeGenerator the strategy used to generate challenge strings
     */
    public InMemoryChallengeService(Duration challengeLifetime, int maxChallenges, ChallengeGenerator challengeGenerator) {
        Assert.notNull(challengeLifetime, "challengeLifetime must not be null");
        Assert.isTrue(maxChallenges > 0, "maxChallenges must be positive");
        Assert.notNull(challengeGenerator, "challengeGenerator must not be null");
        this.challengeLifetime = challengeLifetime;
        this.maxChallenges = maxChallenges;
        this.challengeGenerator = challengeGenerator;
    }

    @Override
    public GeneratedChallenge generateChallenge() {
        cleanupExpired();

        if (this.challenges.size() >= this.maxChallenges) {
            throw new IllegalStateException(
                    "Maximum number of active challenges (" + this.maxChallenges + ") reached. Please try again later.");
        }

        String id = UUID.randomUUID().toString();
        String challenge = this.challengeGenerator.generateChallenge();

        this.challenges.put(id, new ChallengeEntry(challenge, Instant.now().plus(this.challengeLifetime)));
        return new GeneratedChallenge(id, challenge);
    }

    @Override
    public String consumeChallenge(String challengeId) {
        ChallengeEntry entry = this.challenges.remove(challengeId);
        if (entry != null && Instant.now().isBefore(entry.expiresAt)) {
            return entry.challenge;
        }
        return null;
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, ChallengeEntry>> it = this.challenges.entrySet().iterator();
        while (it.hasNext()) {
            if (now.isAfter(it.next().getValue().expiresAt)) {
                it.remove();
            }
        }
    }

    private static class ChallengeEntry {
        final String challenge;
        final Instant expiresAt;

        ChallengeEntry(String challenge, Instant expiresAt) {
            this.challenge = challenge;
            this.expiresAt = expiresAt;
        }
    }
}
