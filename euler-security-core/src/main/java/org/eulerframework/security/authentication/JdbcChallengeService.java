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

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

/**
 * JDBC-backed implementation of {@link ChallengeService}.
 * <p>
 * Challenges are stored in a database table with an expiration timestamp and client binding.
 * Expired entries are cleaned up lazily on each {@link #generateChallenge(String)} call.
 * <p>
 * Requires the following table:
 * <pre>
 * CREATE TABLE oauth2_challenge (
 *     challenge   VARCHAR(64)  PRIMARY KEY,
 *     client_id   VARCHAR(256) NOT NULL,
 *     expires_at  TIMESTAMP    NOT NULL
 * );
 * </pre>
 */
public class JdbcChallengeService implements ChallengeService {

    private static final String DEFAULT_TABLE_NAME = "oauth2_challenge";
    private static final java.util.regex.Pattern TABLE_NAME_PATTERN = java.util.regex.Pattern.compile("^[a-zA-Z0-9_]+$");

    private static final String COLUMN_CHALLENGE = "challenge";
    private static final String COLUMN_CLIENT_ID = "client_id";
    private static final String COLUMN_EXPIRES_AT = "expires_at";

    private final JdbcOperations jdbcOperations;
    private final ChallengeGenerator challengeGenerator;
    private final Duration challengeLifetime;

    private String tableName = DEFAULT_TABLE_NAME;

    /**
     * Create an instance with a 5-minute challenge lifetime and the default
     * {@link Base64UrlChallengeGenerator}.
     *
     * @param jdbcOperations the JDBC operations to use
     */
    public JdbcChallengeService(JdbcOperations jdbcOperations) {
        this(jdbcOperations, Duration.ofMinutes(5));
    }

    /**
     * Create an instance with the specified challenge lifetime and the default
     * {@link Base64UrlChallengeGenerator}.
     *
     * @param jdbcOperations    the JDBC operations to use
     * @param challengeLifetime how long a challenge remains valid
     */
    public JdbcChallengeService(JdbcOperations jdbcOperations, Duration challengeLifetime) {
        this(jdbcOperations, challengeLifetime, new Base64UrlChallengeGenerator());
    }

    /**
     * Create an instance with the specified challenge lifetime and a custom
     * {@link ChallengeGenerator}.
     *
     * @param jdbcOperations     the JDBC operations to use
     * @param challengeLifetime  how long a challenge remains valid
     * @param challengeGenerator the strategy used to generate challenge strings
     */
    public JdbcChallengeService(JdbcOperations jdbcOperations, Duration challengeLifetime, ChallengeGenerator challengeGenerator) {
        Assert.notNull(jdbcOperations, "jdbcOperations must not be null");
        Assert.notNull(challengeLifetime, "challengeLifetime must not be null");
        Assert.notNull(challengeGenerator, "challengeGenerator must not be null");
        this.jdbcOperations = jdbcOperations;
        this.challengeLifetime = challengeLifetime;
        this.challengeGenerator = challengeGenerator;
    }

    @Override
    public GeneratedChallenge generateChallenge(String clientId) {
        Assert.hasText(clientId, "clientId must not be empty");
        cleanupExpired();

        String challenge = this.challengeGenerator.generateChallenge();

        Instant expiresAt = Instant.now().plus(this.challengeLifetime);
        this.jdbcOperations.update(
                "INSERT INTO " + this.tableName + " (" + COLUMN_CHALLENGE + ", " + COLUMN_CLIENT_ID + ", " + COLUMN_EXPIRES_AT + ") VALUES (?, ?, ?)",
                challenge, clientId, Timestamp.from(expiresAt));
        return new GeneratedChallenge(challenge);
    }

    @Override
    public boolean consumeChallenge(String challenge, String clientId) {
        Assert.hasText(clientId, "clientId must not be empty");
        int deleted = this.jdbcOperations.update(
                "DELETE FROM " + this.tableName + " WHERE " + COLUMN_CHALLENGE + " = ? AND " + COLUMN_CLIENT_ID + " = ? AND " + COLUMN_EXPIRES_AT + " > ?",
                challenge, clientId, Timestamp.from(Instant.now()));
        return deleted > 0;
    }

    public void setTableName(String tableName) {
        Assert.hasText(tableName, "tableName must not be empty");
        if (!TABLE_NAME_PATTERN.matcher(tableName).matches()) {
            throw new IllegalArgumentException("tableName must only contain alphanumeric characters and underscores");
        }
        this.tableName = tableName;
    }

    private void cleanupExpired() {
        this.jdbcOperations.update(
                "DELETE FROM " + this.tableName + " WHERE " + COLUMN_EXPIRES_AT + " <= ?",
                Timestamp.from(Instant.now()));
    }
}
