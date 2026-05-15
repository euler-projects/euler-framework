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
package org.eulerframework.security.authentication.otp;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

/**
 * JDBC-backed {@link OtpTicketService} that persists tickets in a relational
 * database.
 * <p>
 * By default this implementation uses the table {@code otp_ticket} with the
 * following schema (compatible with MySQL / MariaDB / PostgreSQL):
 * <pre>{@code
 * CREATE TABLE otp_ticket (
 *     ticket_id              VARCHAR(64)   NOT NULL PRIMARY KEY,
 *     channel                VARCHAR(64)   NOT NULL,
 *     recipient              VARCHAR(255)  NOT NULL,
 *     purpose                VARCHAR(64)   NULL,
 *     otp                    VARCHAR(32)   NOT NULL,
 *     code_challenge         VARCHAR(255)  NOT NULL,
 *     code_challenge_method  VARCHAR(16)   NOT NULL,
 *     expires_at             TIMESTAMP(3)  NOT NULL,
 *     failure_count          INT           NOT NULL DEFAULT 0,
 *     consumed_at            TIMESTAMP(3)  NULL,
 *     created_at             TIMESTAMP(3)  NOT NULL
 * );
 * }</pre>
 * <p>
 * The column names {@code code_challenge} / {@code code_challenge_method}
 * follow PKCE (RFC 7636) standard naming.
 *
 * @see InMemoryOtpTicketService
 * @see RedisOtpTicketService
 */
public class JdbcOtpTicketService implements OtpTicketService {

    public static final int DEFAULT_MAX_FAILURES = 5;

    // @formatter:off
    public static final String DEFAULT_TABLE_NAME = "otp_ticket";

    private static final String COLUMN_TICKET_ID             = "ticket_id";
    private static final String COLUMN_CHANNEL               = "channel";
    private static final String COLUMN_RECIPIENT             = "recipient";
    private static final String COLUMN_PURPOSE               = "purpose";
    private static final String COLUMN_OTP                   = "otp";
    private static final String COLUMN_CODE_CHALLENGE        = "code_challenge";
    private static final String COLUMN_CODE_CHALLENGE_METHOD = "code_challenge_method";
    private static final String COLUMN_EXPIRES_AT            = "expires_at";
    private static final String COLUMN_FAILURE_COUNT         = "failure_count";
    private static final String COLUMN_CONSUMED_AT           = "consumed_at";
    private static final String COLUMN_CREATED_AT            = "created_at";
    // @formatter:on

    private static final String INSERT_TICKET_SQL =
            "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_TICKET_SQL =
            "SELECT %s, %s, %s, %s, %s, %s, %s, %s, %s, %s FROM %s WHERE %s = ?";

    private static final String DELETE_TICKET_SQL =
            "DELETE FROM %s WHERE %s = ?";

    private static final String CONSUME_TICKET_SQL =
            "UPDATE %s SET %s = ? WHERE %s = ? AND %s IS NULL";

    private static final String INCREMENT_FAILURE_SQL =
            "UPDATE %s SET %s = %s + 1 WHERE %s = ? AND %s IS NULL";

    private final JdbcOperations jdbcOperations;
    private final int maxFailures;
    private final String insertSql;
    private final String selectSql;
    private final String deleteSql;
    private final String consumeSql;
    private final String incrementFailureSql;

    public JdbcOtpTicketService(JdbcOperations jdbcOperations) {
        this(jdbcOperations, DEFAULT_TABLE_NAME, DEFAULT_MAX_FAILURES);
    }

    public JdbcOtpTicketService(JdbcOperations jdbcOperations, String tableName, int maxFailures) {
        Assert.notNull(jdbcOperations, "jdbcOperations must not be null");
        Assert.hasText(tableName, "tableName must not be empty");
        Assert.isTrue(maxFailures > 0, "maxFailures must be positive");
        this.jdbcOperations = jdbcOperations;
        this.maxFailures = maxFailures;
        this.insertSql = String.format(INSERT_TICKET_SQL, tableName,
                COLUMN_TICKET_ID, COLUMN_CHANNEL, COLUMN_RECIPIENT, COLUMN_PURPOSE,
                COLUMN_OTP, COLUMN_CODE_CHALLENGE, COLUMN_CODE_CHALLENGE_METHOD,
                COLUMN_EXPIRES_AT, COLUMN_FAILURE_COUNT, COLUMN_CONSUMED_AT, COLUMN_CREATED_AT);
        this.selectSql = String.format(SELECT_TICKET_SQL,
                COLUMN_TICKET_ID, COLUMN_CHANNEL, COLUMN_RECIPIENT, COLUMN_PURPOSE,
                COLUMN_OTP, COLUMN_CODE_CHALLENGE, COLUMN_CODE_CHALLENGE_METHOD,
                COLUMN_EXPIRES_AT, COLUMN_FAILURE_COUNT, COLUMN_CONSUMED_AT,
                tableName, COLUMN_TICKET_ID);
        this.deleteSql = String.format(DELETE_TICKET_SQL, tableName, COLUMN_TICKET_ID);
        this.consumeSql = String.format(CONSUME_TICKET_SQL, tableName,
                COLUMN_CONSUMED_AT, COLUMN_TICKET_ID, COLUMN_CONSUMED_AT);
        this.incrementFailureSql = String.format(INCREMENT_FAILURE_SQL, tableName,
                COLUMN_FAILURE_COUNT, COLUMN_FAILURE_COUNT,
                COLUMN_TICKET_ID, COLUMN_CONSUMED_AT);
    }

    @Override
    public void save(OtpTicket ticket) {
        Assert.notNull(ticket, "ticket must not be null");
        Timestamp now = Timestamp.from(Instant.now());
        Timestamp expiresAt = Timestamp.from(ticket.expiresAt());
        this.jdbcOperations.update(this.insertSql, ps -> {
            int i = 0;
            ps.setString(++i, ticket.ticketId());
            ps.setString(++i, ticket.channel());
            ps.setString(++i, ticket.recipient());
            ps.setString(++i, ticket.purpose());
            ps.setString(++i, ticket.otp());
            ps.setString(++i, ticket.codeChallenge());
            ps.setString(++i, ticket.codeChallengeMethod());
            ps.setTimestamp(++i, expiresAt);
            ps.setInt(++i, ticket.failureCount());
            if (ticket.consumed()) {
                ps.setTimestamp(++i, now);
            } else {
                ps.setNull(++i, java.sql.Types.TIMESTAMP);
            }
            ps.setTimestamp(++i, now);
        });
    }

    @Override
    public OtpVerification consume(String ticketId, String codeVerifier, String otp, String expectedPurpose) {
        if (ticketId == null) {
            return null;
        }
        OtpTicket ticket = this.jdbcOperations.query(this.selectSql,
                ps -> ps.setString(1, ticketId),
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    Timestamp consumedAt = rs.getTimestamp(COLUMN_CONSUMED_AT);
                    return new OtpTicket(
                            rs.getString(COLUMN_TICKET_ID),
                            rs.getString(COLUMN_CHANNEL),
                            rs.getString(COLUMN_RECIPIENT),
                            rs.getString(COLUMN_PURPOSE),
                            rs.getString(COLUMN_OTP),
                            rs.getString(COLUMN_CODE_CHALLENGE),
                            rs.getString(COLUMN_CODE_CHALLENGE_METHOD),
                            rs.getTimestamp(COLUMN_EXPIRES_AT).toInstant(),
                            rs.getInt(COLUMN_FAILURE_COUNT),
                            consumedAt != null);
                });

        if (ticket == null || ticket.consumed() || Instant.now().isAfter(ticket.expiresAt())) {
            this.jdbcOperations.update(this.deleteSql, ticketId);
            return null;
        }

        boolean otpMatches = Objects.equals(ticket.otp(), otp);
        boolean pkceMatches = OtpPkceVerifier.verify(codeVerifier, ticket.codeChallenge(), ticket.codeChallengeMethod());
        boolean purposeMatches = expectedPurpose == null
                || Objects.equals(expectedPurpose, ticket.purpose());

        if (otpMatches && pkceMatches && purposeMatches) {
            Timestamp now = Timestamp.from(Instant.now());
            int updated = this.jdbcOperations.update(this.consumeSql, now, ticketId);
            if (updated == 1) {
                return new OtpVerification(ticket.ticketId(), ticket.channel(), ticket.recipient(),
                        ticket.purpose(), now.toInstant());
            }
            return null;
        }

        // Failure path
        if (ticket.failureCount() + 1 >= this.maxFailures) {
            this.jdbcOperations.update(this.deleteSql, ticketId);
        } else {
            this.jdbcOperations.update(this.incrementFailureSql, ticketId);
        }
        return null;
    }
}
