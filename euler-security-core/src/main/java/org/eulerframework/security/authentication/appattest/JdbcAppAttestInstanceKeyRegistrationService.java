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
 * JDBC implementation of {@link AppAttestInstanceKeyRegistrationService} that persists the
 * public keys App instances register in a relational database.
 * <p>
 * By default, this implementation uses the table {@code app_attest_instance_key_registration}
 * with the following schema:
 * <pre>{@code
 * CREATE TABLE app_attest_instance_key_registration (
 *     jwk_kid        VARCHAR(128) NOT NULL,
 *     app_attest_kid VARCHAR(255) NOT NULL,
 *     jwk            TEXT         NOT NULL,
 *     created_date   DATETIME(3)  NOT NULL,
 *     PRIMARY KEY (jwk_kid),
 *     KEY idx_app_attest_instance_key_app_attest_kid (app_attest_kid)
 * );
 * }</pre>
 * <p>
 * {@code app_attest_kid} is the same value {@code app_attest_attestation_registration.key_id}
 * holds, and is called that here rather than {@code key_id} because this row also names a second
 * key: {@code jwk_kid} is the {@code kid} of the registered public key, and the two are different
 * keys in different namespaces. Qualifying both is what keeps a reader from taking one for the
 * other.
 * <p>
 * The primary key is {@code jwk_kid} alone: a key ID is unique across the whole registry and not
 * merely within one instance. A lookup still names both halves, because a consumer may only read
 * the keys of the instance it has authenticated, and {@code app_attest_kid} in the WHERE clause
 * is what says so.
 * <p>
 * {@link #saveRegistration} is insert-only. A collision on the primary key is reported rather than
 * settled by overwriting: the row already there may belong to another instance, and replacing it
 * could discard the key an account is bound to. Inserting and letting the database refuse is also
 * what keeps two concurrent registrations of one key ID from both believing they won. Every call
 * ends by reading the row back, so what a caller reports is what was persisted and not what was
 * handed in.
 * <p>
 * The {@code jwk} column has to hold the JWK as submitted <b>and hand it back verbatim</b>, which
 * is what TEXT does and what rules out the two tempting alternatives. A column too narrow is either
 * rejected outright by a strict SQL mode or silently shortened by one configured to truncate,
 * leaving a row no consumer can parse; and a type that re-serialises on read &mdash; MySQL's
 * {@code json}, which sorts object keys and normalises whitespace &mdash; returns something that is
 * not what was written, so the read-back below disagrees with it every time. Whichever way the store
 * falls short, {@link #saveRegistration} reports it at the registration that caused it rather than leaving an
 * unusable key to surface at some later use.
 *
 * @see AppAttestInstanceKeyRegistrationService
 * @see InMemoryAppAttestInstanceKeyRegistrationService
 */
public class JdbcAppAttestInstanceKeyRegistrationService implements AppAttestInstanceKeyRegistrationService {

    // @formatter:off
    private static final String DEFAULT_TABLE_NAME = "app_attest_instance_key_registration";

    private static final String COLUMN_JWK_KID        = "jwk_kid";
    private static final String COLUMN_APP_ATTEST_KID = "app_attest_kid";
    private static final String COLUMN_JWK            = "jwk";
    private static final String COLUMN_CREATED_DATE   = "created_date";
    // @formatter:on

    private static final String INSERT_KEY_SQL =
            "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)";

    private static final String SELECT_KEY_SQL =
            "SELECT %s, %s, %s FROM %s WHERE %s = ? AND %s = ?";

    private final JdbcOperations jdbcOperations;
    private final String insertSql;
    private final String selectSql;

    /**
     * Create a new {@code JdbcAppAttestInstanceKeyRegistrationService} with the default table name.
     *
     * @param jdbcOperations the JDBC operations (must not be {@code null})
     */
    public JdbcAppAttestInstanceKeyRegistrationService(JdbcOperations jdbcOperations) {
        this(jdbcOperations, DEFAULT_TABLE_NAME);
    }

    /**
     * Create a new {@code JdbcAppAttestInstanceKeyRegistrationService} with a custom table name.
     *
     * @param jdbcOperations the JDBC operations (must not be {@code null})
     * @param tableName      the table name to use (must not be empty)
     */
    public JdbcAppAttestInstanceKeyRegistrationService(JdbcOperations jdbcOperations, String tableName) {
        Assert.notNull(jdbcOperations, "jdbcOperations must not be null");
        Assert.hasText(tableName, "tableName must not be empty");
        this.jdbcOperations = jdbcOperations;
        this.insertSql = String.format(INSERT_KEY_SQL, tableName,
                COLUMN_JWK_KID, COLUMN_APP_ATTEST_KID, COLUMN_JWK, COLUMN_CREATED_DATE);
        this.selectSql = String.format(SELECT_KEY_SQL,
                COLUMN_APP_ATTEST_KID, COLUMN_JWK_KID, COLUMN_JWK,
                tableName, COLUMN_JWK_KID, COLUMN_APP_ATTEST_KID);
    }

    @Override
    public AppAttestInstanceKeyRegistration saveRegistration(AppAttestInstanceKeyRegistration registration) {
        Assert.notNull(registration, "registration must not be null");
        Timestamp now = Timestamp.from(Instant.now());
        try {
            this.jdbcOperations.update(this.insertSql, ps -> {
                ps.setString(1, registration.jwkKid());
                ps.setString(2, registration.appAttestKid());
                ps.setString(3, registration.jwk());
                ps.setTimestamp(4, now);
            });
        } catch (DuplicateKeyException e) {
            // Taken. Reported rather than overwritten: the row already there may belong to another
            // instance, and even when it does not, replacing it could discard the key an account is
            // bound to and leave that account unreachable with nothing to detect the cause by.
            throw new DuplicateInstanceKeyException(registration.jwkKid());
        }

        // Read back rather than return the argument, and check it against what was written.
        // A store that cannot round-trip the value is the one failure this has to catch
        // itself: a column too narrow is rejected outright by a strict SQL mode but silently
        // shortened by one configured to truncate, and a type that re-serialises on read (a
        // MySQL json column, say) returns something that is not what went in. Either way the
        // registry would be left holding a key nothing can use, so it is reported here, at
        // the registration that caused it, rather than at the next request that fails.
        AppAttestInstanceKeyRegistration stored =
                findByAppAttestKidAndJwkKid(registration.appAttestKid(), registration.jwkKid());
        if (stored == null) {
            throw new IllegalStateException("Instance key was not stored (appAttestKid='"
                    + registration.appAttestKid() + "', jwkKid='" + registration.jwkKid() + "')");
        }
        if (!registration.jwk().equals(stored.jwk())) {
            throw new IllegalStateException("Instance key was not stored as submitted (appAttestKid='"
                    + registration.appAttestKid() + "', jwkKid='" + registration.jwkKid()
                    + "'): the jwk column did not return the value written, which means it is too narrow for "
                    + registration.jwk().length() + " characters or does not store text verbatim");
        }
        return stored;
    }

    @Override
    public AppAttestInstanceKeyRegistration findByAppAttestKidAndJwkKid(String appAttestKid, String jwkKid) {
        return this.jdbcOperations.query(this.selectSql,
                ps -> {
                    ps.setString(1, jwkKid);
                    ps.setString(2, appAttestKid);
                },
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    return new AppAttestInstanceKeyRegistration(
                            rs.getString(COLUMN_APP_ATTEST_KID),
                            rs.getString(COLUMN_JWK_KID),
                            rs.getString(COLUMN_JWK));
                });
    }
}
