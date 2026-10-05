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

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * JDBC implementation of {@link AppAttestIssuedKeyService} that persists issued public
 * keys in a relational database.
 * <p>
 * By default, this implementation uses the table {@code app_attest_issued_key} with the
 * following schema:
 * <pre>{@code
 * CREATE TABLE app_attest_issued_key (
 *     issuer        VARCHAR(255) NOT NULL,
 *     key_id        VARCHAR(255) NOT NULL,
 *     jwk           TEXT         NOT NULL,
 *     created_date  DATETIME(3)  NOT NULL,
 *     modified_date DATETIME(3)  NOT NULL,
 *     PRIMARY KEY (issuer, key_id)
 * );
 * }</pre>
 * <p>
 * The composite primary key is what makes {@link #saveKey} idempotent: the insert of an
 * already-registered pair is rejected by the database and turned into an update of that row,
 * so a repeated registration neither adds an entry nor fails. Doing it against the database
 * rather than with a read-then-write keeps two concurrent registrations of the same key from
 * both believing they were first. Every call ends by reading the row back, so what a caller
 * reports is what was persisted and not what was handed in.
 * <p>
 * The {@code jwk} column has to hold the JWK as submitted <b>and hand it back verbatim</b>,
 * which is what TEXT does and what rules out the two tempting alternatives. A column too
 * narrow is either rejected outright by a strict SQL mode or silently shortened by one
 * configured to truncate, leaving a row no login can parse; and a type that re-serialises on
 * read &mdash; MySQL's {@code json}, which sorts object keys and normalises whitespace &mdash;
 * returns something that is not what was written, so the read-back below disagrees with it
 * every time. Whichever way the store falls short, {@link #saveKey} reports it at the
 * registration that caused it rather than leaving an unusable key to surface at some later
 * login.
 *
 * @see AppAttestIssuedKeyService
 * @see InMemoryAppAttestIssuedKeyService
 */
public class JdbcAppAttestIssuedKeyService implements AppAttestIssuedKeyService {

    // @formatter:off
    private static final String DEFAULT_TABLE_NAME = "app_attest_issued_key";

    private static final String COLUMN_ISSUER        = "issuer";
    private static final String COLUMN_KEY_ID        = "key_id";
    private static final String COLUMN_JWK           = "jwk";
    private static final String COLUMN_CREATED_DATE  = "created_date";
    private static final String COLUMN_MODIFIED_DATE = "modified_date";
    // @formatter:on

    private static final String INSERT_KEY_SQL =
            "INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_KEY_SQL =
            "UPDATE %s SET %s = ?, %s = ? WHERE %s = ? AND %s = ?";

    private static final String SELECT_KEY_SQL =
            "SELECT %s, %s, %s FROM %s WHERE %s = ? AND %s = ?";

    private final JdbcOperations jdbcOperations;
    private final String insertSql;
    private final String updateSql;
    private final String selectSql;

    /**
     * Create a new {@code JdbcAppAttestIssuedKeyService} with the default table name.
     *
     * @param jdbcOperations the JDBC operations (must not be {@code null})
     */
    public JdbcAppAttestIssuedKeyService(JdbcOperations jdbcOperations) {
        this(jdbcOperations, DEFAULT_TABLE_NAME);
    }

    /**
     * Create a new {@code JdbcAppAttestIssuedKeyService} with a custom table name.
     *
     * @param jdbcOperations the JDBC operations (must not be {@code null})
     * @param tableName      the table name to use (must not be empty)
     */
    public JdbcAppAttestIssuedKeyService(JdbcOperations jdbcOperations, String tableName) {
        Assert.notNull(jdbcOperations, "jdbcOperations must not be null");
        Assert.hasText(tableName, "tableName must not be empty");
        this.jdbcOperations = jdbcOperations;
        this.insertSql = String.format(INSERT_KEY_SQL, tableName,
                COLUMN_ISSUER, COLUMN_KEY_ID, COLUMN_JWK,
                COLUMN_CREATED_DATE, COLUMN_MODIFIED_DATE);
        // created_date is deliberately absent: it records when the issuer first vouched for
        // this key, which a later registration of the same key does not change.
        this.updateSql = String.format(UPDATE_KEY_SQL, tableName,
                COLUMN_JWK, COLUMN_MODIFIED_DATE,
                COLUMN_ISSUER, COLUMN_KEY_ID);
        this.selectSql = String.format(SELECT_KEY_SQL,
                COLUMN_ISSUER, COLUMN_KEY_ID, COLUMN_JWK,
                tableName, COLUMN_ISSUER, COLUMN_KEY_ID);
    }

    @Override
    public AppAttestIssuedKey saveKey(AppAttestIssuedKey key) {
        Assert.notNull(key, "key must not be null");
        Timestamp now = Timestamp.from(Instant.now());
        try {
            this.jdbcOperations.update(this.insertSql, ps -> {
                int index = 0;
                ps.setString(++index, key.issuer());
                ps.setString(++index, key.keyId());
                ps.setString(++index, key.jwk());
                ps.setTimestamp(++index, now);
                ps.setTimestamp(++index, now);
            });
        } catch (DuplicateKeyException e) {
            // Already registered. Overwrite rather than keep the first: the key ID is the
            // thumbprint of the key material, so the shared (issuer, keyId) proves the two
            // submissions are the same key and only metadata the thumbprint does not cover
            // (alg, use, an x5c chain) can differ. Nothing verifies against those, so the
            // registry is better off saying what the issuer last told it than what it said
            // first - and a client that reserialises its key slightly differently is not
            // thereby stuck with a registration that no longer describes what it holds.
            this.jdbcOperations.update(this.updateSql, ps -> {
                ps.setString(1, key.jwk());
                ps.setTimestamp(2, now);
                ps.setString(3, key.issuer());
                ps.setString(4, key.keyId());
            });
        }

        // Read back rather than return the argument, and check it against what was written.
        // A store that cannot round-trip the value is the one failure this has to catch
        // itself: a column too narrow is rejected outright by a strict SQL mode but silently
        // shortened by one configured to truncate, and a type that re-serialises on read (a
        // MySQL json column, say) returns something that is not what went in. Either way the
        // registry would be left holding a key no login can use, so it is reported here, at
        // the registration that caused it, rather than at the next assertion that fails.
        AppAttestIssuedKey stored = findByIssuerAndKeyId(key.issuer(), key.keyId());
        if (stored == null) {
            throw new IllegalStateException("Issued key was not stored (issuer='" + key.issuer()
                    + "', keyId='" + key.keyId() + "')");
        }
        if (!key.jwk().equals(stored.jwk())) {
            throw new IllegalStateException("Issued key was not stored as submitted (issuer='" + key.issuer()
                    + "', keyId='" + key.keyId() + "'): the jwk column did not return the value written, "
                    + "which means it is too narrow for " + key.jwk().length() + " characters or does not "
                    + "store text verbatim");
        }
        return stored;
    }

    @Override
    public AppAttestIssuedKey findByIssuerAndKeyId(String issuer, String keyId) {
        return this.jdbcOperations.query(this.selectSql,
                ps -> {
                    ps.setString(1, issuer);
                    ps.setString(2, keyId);
                },
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    return new AppAttestIssuedKey(
                            rs.getString(COLUMN_ISSUER),
                            rs.getString(COLUMN_KEY_ID),
                            rs.getString(COLUMN_JWK));
                });
    }
}
